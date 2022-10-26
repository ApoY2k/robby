package apoy2k.robby.engine

import apoy2k.robby.exceptions.IncompleteAction
import apoy2k.robby.exceptions.InvalidGameState
import apoy2k.robby.model.*
import apoy2k.robby.model.predef.board.generateChopShopBoard
import apoy2k.robby.model.predef.board.generateDemoBoard
import apoy2k.robby.model.predef.board.generateSandboxBoard
import apoy2k.robby.model.predef.board.linkBoard
import io.ktor.server.html.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.*
import org.slf4j.LoggerFactory
import java.lang.Integer.max
import java.lang.Integer.min
import java.time.Clock

private const val GAME_ENGINE_STEP_DELAY = 1000L

/**
 * Game engine to advance games based on incoming actions
 */
class GameEngine(
    private val clock: Clock,
    private val database: Database,
    private val robotEngine: RobotEngine,
    private val updates: MutableSharedFlow<ViewUpdate>
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Starts listening to message on the command channel and executes them on the corresponding game,
     * sending back viewupdates for the game along the way
     */
    suspend fun connect(actions: Flow<Action>) = coroutineScope {
        actions.onEach { action ->
            val game = action.game
            if (game == null) {
                logger.warn("Could not find game corresponding to $action")
                return@onEach
            }

            if (clock.instant().isAfter(game.finishedAt)) {
                logger.warn("$game is already finished! Ignorning $action")
                return@onEach
            }

            try {
                logger.debug("Received $action")

                // Perform the action and run automatic registers and actions
                // with ViewUpdates in a separate coroutine so the channel isn't blocked
                launch {
                    perform(action)
                    updates.emit(ViewUpdate(game.id))
                    runEngine(game)
                }
            } catch (err: Throwable) {
                logger.error("Error executing $action: $err", err)
            }
        }.launchIn(this)
    }

    /**
     * Advances the game state by executing a single action, without sending ViewUpdates
     */
    fun perform(action: Action) {
        // All actions require a session, so if an action without a session is noticed, just drop it with
        // an empty result view update set
        val session = action.session ?: throw IncompleteAction("No session attached to $action")
        val game = action.game ?: throw IncompleteAction("No game attached to $action")

        if (action.label == ActionLabel.JOIN_GAME) {
            val model = action.getString(ActionField.ROBOT_MODEL) ?: throw IncompleteAction("No robot model specified")
            addPlayer(game, session.name, RobotModel.valueOf(model), session)
            return
        }

        val robot = database.robots.find { it.session eq session.id }
        if (robot == null) {
            logger.warn("No player found for $session")
            return
        }

        when (action.label) {
            ActionLabel.LEAVE_GAME -> removeRobot(game, robot)
            ActionLabel.TOGGLE_READY -> robot.toggleReady()
            ActionLabel.TOGGLE_POWERDOWN -> robot.togglePowerDown()

            ActionLabel.SELECT_CARD -> {
                val cardId = action.getInt(ActionField.CARD_ID) ?: throw IncompleteAction("No cardId defined")
                val register = action.getInt(ActionField.REGISTER) ?: throw IncompleteAction("No register defined")
                robotEngine.selectCard(robot, register, cardId)
            }

            else -> {
                logger.warn("No executor for $action")
            }
        }
    }

    /**
     * Create a new game and return the created instance
     */
    fun createNewGame(type: BoardType): Game {
        val game = Game {
            state = GameState.PROGRAMMING_REGISTERS
            currentRegister = 1
            startedAt = clock.instant()
            finishedAt = null
        }

        val fields = when (type) {
            BoardType.CHOPSHOP -> generateChopShopBoard()
            BoardType.DEMO -> generateDemoBoard()
            BoardType.SANDBOX -> generateSandboxBoard()
        }

        val dbFields = linkBoard(game, fields)

        return database.useTransaction {
            database.games.add(game)
            dbFields
                .forEach {
                    database.fields.add(it)
                }
            game
        }
    }

    /**
     * Run all automatic steps in the game state (executed between each register)
     */
    private suspend fun runAutomaticSteps(game: Game) {
        runMoveBoardElements(game)
        runFireLasers(game)
        runCheckpoints(game)
        runRepairPowerups(game)
    }

    /**
     * Run engine on game state, sends view updates accordingly
     */
    private suspend fun runEngine(game: Game) {
        val robots = game.robots(database)

        // Check conditions for running automatic registers are fulfilled
        if (robots.isEmpty() || robots.any { !it.ready }) {
            return
        }

        if (game.isFinished(clock.instant())) {
            return
        }

        val now = clock.instant()
        if (!game.hasStarted(now)) {
            game.startedAt = now
        }

        // Send view updates between registers so players see the new state after "preparing" the execution of movements
        runRegister(game, 1)
        runAutomaticSteps(game)

        runRegister(game, 2)
        runAutomaticSteps(game)

        runRegister(game, 3)
        runAutomaticSteps(game)

        runRegister(game, 4)
        runAutomaticSteps(game)

        runRegister(game, 5)
        runAutomaticSteps(game)

        // Check for game end condition. If it is met, end the game
        if (robots.any { it.passedCheckpoints >= 3 }) {
            game.finishedAt = clock.instant()
        } else {
            // After all automatic turns are done, deal new cards so players can plan their next move
            game.state = GameState.PROGRAMMING_REGISTERS
            robots.forEach {
                robotEngine.prepareNewRound(game, it)
            }
        }

        updates.emit(ViewUpdate(game.id))
    }

    /**
     * Get all movement cards of a specific register, sorted, for all players robots
     */
    private fun getRegister(gameId: Int, register: Int): List<MovementCard> {
        return database.cards
            .filter { (it.gameId eq gameId) and (it.register eq register) }
            .sortedBy { it.priority }
            .map { it }
    }

    /**
     * Run through all movement cards of a register in their prioritized order with delay.
     * Sends view updates after every movement.
     * Movements of more than 1 step are executed individually, with updates sent between every step
     */
    private suspend fun runRegister(game: Game, register: Int) {
        game.state = GameState.EXECUTING_REGISTERS
        game.currentRegister = register
        updates.emit(ViewUpdate(game.id))

        try {
            getRegister(game.id, register)
                .forEach {
                    val steps = when (it.movement) {
                        Movement.STRAIGHT_2 -> 2
                        Movement.STRAIGHT_3 -> 3
                        else -> 1
                    }

                    for (step in 1..steps) {
                        game.board.execute(it)
                        updates.emit(ViewUpdate(game.id))
                        delay(GAME_ENGINE_STEP_DELAY)
                    }
                }
        } catch (err: Throwable) {
            logger.error("Error executing register: ${err.message}", err)
        }
    }

    private suspend fun runMoveBoardElements(game: Game) {
        game.state = GameState.MOVE_BARD_ELEMENTS_2
        game.board.moveBelts(FieldType.BELT_2)
        updates.emit(ViewUpdate(game.id))
        delay(GAME_ENGINE_STEP_DELAY)

        game.state = GameState.MOVE_BARD_ELEMENTS_1
        game.board.moveBelts(FieldType.BELT)
        updates.emit(ViewUpdate(game.id))
        delay(GAME_ENGINE_STEP_DELAY)
    }

    private suspend fun runFireLasers(game: Game) {
        game.state = GameState.FIRE_LASERS_2
        game.board.fireLasers(FieldType.LASER_2)
        updates.emit(ViewUpdate(game.id))
        delay(GAME_ENGINE_STEP_DELAY)

        game.state = GameState.FIRE_LASERS_1
        game.board.fireLasers(FieldType.LASER)
        updates.emit(ViewUpdate(game.id))
        delay(GAME_ENGINE_STEP_DELAY)
    }

    private suspend fun runCheckpoints(game: Game) {
        game.state = GameState.CHECKPOINTS
        game.board.fields.flatten()
            .filter { it.type == FieldType.FLAG }
            .mapNotNull { it.robot }
            .forEach { it.passedCheckpoints = min(3, it.passedCheckpoints + 1) }
        // TODO: Touching the same checkpoint multiple times should not increase the counter
        updates.emit(ViewUpdate(game.di))
        delay(GAME_ENGINE_STEP_DELAY)
    }

    private suspend fun runRepairPowerups(game: Game) {
        game.state = GameState.REPAIR_POWERUPS
        game.board.fields.flatten()
            .filter { it.type == FieldType.REPAIR }
            .mapNotNull { it.robot }
            .forEach { it.damage = max(0, it.damage - 1) }

        // TODO Implement powerups

        updates.emit(ViewUpdate(game.id))
        delay(GAME_ENGINE_STEP_DELAY)
    }

    private fun removeRobot(game: Game, robot: Robot) {
        database.robots.removeIf { it.id eq robot.id }
    }

    private fun addPlayer(game: Game, name: String, model: RobotModel, session: Session) {
        if (name.isBlank()) throw IncompleteAction("No name provided")
        if (game.players.any { it.session == session }) throw InvalidGameState("$session already joined")

        val robot = Robot.new(model, session).also {
            it.game = game
        }

        database.robots.add(robot)

        robotEngine.drawCards(game, robot)

        game.board.fields.flatten()
            .first { it.robot == null }
            .let { it.robot = robot }
    }
}
