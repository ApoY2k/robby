package apoy2k.robby.engine

import apoy2k.robby.exceptions.IncompleteAction
import apoy2k.robby.model.*
import apoy2k.robby.model.predef.board.generateChopShopBoard
import apoy2k.robby.model.predef.board.generateDemoBoard
import apoy2k.robby.model.predef.board.generateSandboxBoard
import apoy2k.robby.model.predef.deck.generateStandardDeck
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

                val boardEngine = BoardEngine.build(game.id, database)

                // Perform the action and run automatic registers and actions
                // with ViewUpdates in a separate coroutine so the channel isn't blocked
                launch {
                    perform(action, boardEngine)
                    updates.emit(ViewUpdate(game.id))
                    runEngine(game, boardEngine)
                }
            } catch (err: Throwable) {
                logger.error("Error executing $action: $err", err)
            }
        }.launchIn(this)
    }

    /**
     * Advances the game state by executing a single action, without sending ViewUpdates
     */
    fun perform(action: Action, boardEngine: BoardEngine) {
        // All actions require a session, so if an action without a session is noticed, just drop it with
        // an empty result view update set
        val session = action.session ?: throw IncompleteAction("No session attached to $action")
        val game = action.game ?: throw IncompleteAction("No game attached to $action")

        if (action.label == ActionLabel.JOIN_GAME) {
            val model = action.getString(ActionField.ROBOT_MODEL) ?: throw IncompleteAction("No robot model specified")
            val robot = addRobot(game.id, RobotModel.valueOf(model), session)
            boardEngine.placeRobot(robot.id)
            return
        }

        val robot = database.robots.find { it.gameId eq game.id and (it.session eq session.id) }
        if (robot == null) {
            logger.warn("No Robot found for $session in $game")
            return
        }

        when (action.label) {
            ActionLabel.LEAVE_GAME -> removeRobot(robot.id)
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

        return database.useTransaction {
            database.games.add(game)
            database.createFieldsForGame(fields, game.id)
            generateStandardDeck().forEach {
                it.gameId = game.id
                database.cards.add(it)
            }
            game
        }
    }

    /**
     * Run engine on game state, sends view updates accordingly
     */
    private suspend fun runEngine(game: Game, boardEngine: BoardEngine) {
        val robots = game.robots(database).map { it }

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
        runRegister(game, boardEngine, 1)
        runAutomaticSteps(game, boardEngine)

        runRegister(game, boardEngine, 2)
        runAutomaticSteps(game, boardEngine)

        runRegister(game, boardEngine, 3)
        runAutomaticSteps(game, boardEngine)

        runRegister(game, boardEngine, 4)
        runAutomaticSteps(game, boardEngine)

        runRegister(game, boardEngine, 5)
        runAutomaticSteps(game, boardEngine)

        // Check for game end condition. If it is met, end the game
        if (robots.any { it.passedCheckpoints >= 3 }) {
            game.finishedAt = clock.instant()
        } else {
            // After all automatic turns are done, deal new cards so players can plan their next move
            game.state = GameState.PROGRAMMING_REGISTERS
            robots.forEach {
                robotEngine.prepareNewRound(game.id, it)
            }
        }

        updates.emit(ViewUpdate(game.id))
    }

    /**
     * Run all automatic steps in the game state (executed between each register)
     */
    private suspend fun runAutomaticSteps(game: Game, boardEngine: BoardEngine) {
        runMoveBoardElements(game, boardEngine)
        runFireLasers(game, boardEngine)
        runCheckpoints(game, boardEngine)
        runRepairPowerups(game, boardEngine)
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
    private suspend fun runRegister(game: Game, boardEngine: BoardEngine, register: Int) {
        game.state = GameState.EXECUTING_REGISTERS
        game.currentRegister = register
        updates.emit(ViewUpdate(game.id))

        try {
            getRegister(game.id, register)
                .forEach { card ->
                    val robot = database.robots.find { it.id eq card.id }
                        ?: return@forEach

                    val steps = when (card.movement) {
                        Movement.STRAIGHT_2 -> 2
                        Movement.STRAIGHT_3 -> 3
                        else -> 1
                    }

                    for (step in 1..steps) {
                        boardEngine.execute(card, robot)
                        updates.emit(ViewUpdate(game.id))
                        delay(GAME_ENGINE_STEP_DELAY)
                    }
                }
        } catch (err: Throwable) {
            logger.error("Error executing register: ${err.message}", err)
        }
    }

    private suspend fun runMoveBoardElements(game: Game, boardEngine: BoardEngine) {
        game.state = GameState.MOVE_BARD_ELEMENTS_2
        val robots = game.robots(database).map { it }
        boardEngine.moveBelts(FieldType.BELT_2, robots)
        updates.emit(ViewUpdate(game.id))
        delay(GAME_ENGINE_STEP_DELAY)

        game.state = GameState.MOVE_BARD_ELEMENTS_1
        boardEngine.moveBelts(FieldType.BELT, robots)
        robots.forEach { database.robots.update(it) }
        updates.emit(ViewUpdate(game.id))
        delay(GAME_ENGINE_STEP_DELAY)
    }

    private suspend fun runFireLasers(game: Game, boardEngine: BoardEngine) {
        game.state = GameState.FIRE_LASERS_2
        val robots = game.robots(database).map { it }
        boardEngine.fireLasers(FieldType.LASER_2, robots)
        updates.emit(ViewUpdate(game.id))
        delay(GAME_ENGINE_STEP_DELAY)

        game.state = GameState.FIRE_LASERS_1
        boardEngine.fireLasers(FieldType.LASER, robots)
        robots.forEach { database.robots.update(it) }
        updates.emit(ViewUpdate(game.id))
        delay(GAME_ENGINE_STEP_DELAY)
    }

    private suspend fun runCheckpoints(game: Game, boardEngine: BoardEngine) {
        game.state = GameState.CHECKPOINTS
        val robots = game.robots(database).map { it }
        boardEngine.touchCheckpoints(robots)
        robots.forEach { database.robots.update(it) }
        updates.emit(ViewUpdate(game.id))
        delay(GAME_ENGINE_STEP_DELAY)
    }

    private suspend fun runRepairPowerups(game: Game, boardEngine: BoardEngine) {
        game.state = GameState.REPAIR_POWERUPS
        val robots = game.robots(database).map { it }
        boardEngine.touchRepair(robots)
        boardEngine.touchModifications()
        robots.forEach { database.robots.update(it) }
        updates.emit(ViewUpdate(game.id))
        delay(GAME_ENGINE_STEP_DELAY)
    }

    private fun removeRobot(robotId: Int) {
        database.robots.removeIf { it.id eq robotId }
    }

    private fun addRobot(gameId: Int, model: RobotModel, session: Session): Robot {
        val robot = robotEngine.createNewRobot(gameId, session, model)
        robotEngine.drawCards(gameId, robot)
        return robot
    }
}
