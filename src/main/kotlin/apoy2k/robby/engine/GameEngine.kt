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
import org.ktorm.dsl.batchUpdate
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

            if (game.isFinished(clock.instant())) {
                logger.warn("$game is already finished! Ignorning $action")
                return@onEach
            }

            try {
                logger.debug("Received $action")

                val board = database.fields.filter { it.gameId eq game.id }.map { it }.toBoard()
                val robots = database.robots.filter { it.gameId eq game.id }.map { it }

                // Perform the action and run automatic registers and actions
                // with ViewUpdates in a separate coroutine so the channel isn't blocked
                launch {
                    perform(action, board, robots)
                    updates.emit(ViewUpdate(game.id))
                    runEngine(game, board, robots)
                }
            } catch (err: Throwable) {
                logger.error("Error executing $action: $err", err)
            }
        }.launchIn(this)
    }

    /**
     * Advances the game state by executing a single action, without sending ViewUpdates
     */
    fun perform(action: Action, board: Board, robots: Collection<Robot>) {
        // All actions require a session and game, so if an action without a session is noticed, just drop it with
        // an empty result view update set
        val session = action.session ?: throw IncompleteAction("No session attached to $action")
        val user = database.users.find { it.id eq (session.userId ?: -1) }
            ?: throw IncompleteAction("No user found for $session")
        val game = action.game ?: throw IncompleteAction("No game attached to $action")

        if (action.label == ActionLabel.JOIN_GAME) {
            val model = action.getString(ActionField.ROBOT_MODEL) ?: throw IncompleteAction("No robot model specified")

            val robotCount = database.robots.count { it.gameId eq game.id }
            if (game.maxRobots <= robotCount) {
                return
            }

            val robot = addRobot(game.id, user.name, user.id, RobotModel.valueOf(model))
            board.placeRobot(robot.id)
            board.flatten().forEach { database.fields.update(it) }
            return
        }

        val robot = robots.firstOrNull { it.userId == user.id }
        if (robot == null) {
            logger.warn("No Robot found for $session in $game")
            return
        }

        when (action.label) {
            ActionLabel.LEAVE_GAME -> removeRobot(robot.id)
            ActionLabel.TOGGLE_READY -> robotEngine.toggleReady(robot)
            ActionLabel.TOGGLE_POWERDOWN -> robotEngine.togglePowerDown(robot)

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
    fun createNewGame(boardType: BoardType, maxRobots: Int): Game {
        val game = Game {
            state = GameState.PROGRAMMING_REGISTERS
            this.boardType = boardType
            this.maxRobots = maxRobots
            currentRegister = 1
            startedAt = null
            finishedAt = null
        }

        val fields = when (boardType) {
            BoardType.CHOPSHOP -> generateChopShopBoard()
            BoardType.DEMO -> generateDemoBoard()
            BoardType.SANDBOX -> generateSandboxBoard()
        }

        database.useTransaction {
            database.games.add(game)

            fields.mapIndexed { row, rowFields ->
                rowFields.mapIndexed { col, field ->
                    field.gameId = game.id
                    field.positionX = col
                    field.positionY = row

                    database.fields.add(field)
                }
            }

            // Applying laser overlays only works correctly when all fields are uniquely adressable with an ID
            // so this has to be done *after* writing the DB entries for fields, as that will set the field IDs
            fields.updateLaserOverlays(emptySet())
            database.batchUpdate(Fields) {
                fields.flatten().forEach { field ->
                    item {
                        set(it.elements, field.elements)
                        where { it.id eq field.id }
                    }
                }
            }

            generateStandardDeck().forEach {
                it.gameId = game.id
                database.cards.add(it)
            }
        }

        return game
    }

    /**
     * Run engine on game state, sends view updates accordingly
     */
    private suspend fun runEngine(game: Game, board: Board, robots: Collection<Robot>) {
        if (game.isFinished(clock.instant())) {
            return
        }

        val allReady = database.robots
            .filter { it.gameId eq game.id }
            .all { it.ready eq true }

        // Check conditions for running automatic registers are fulfilled
        if (!allReady) {
            return
        }

        val now = clock.instant()
        if (!game.hasStarted(now)) {
            logger.info("Starting first round of $game")
            game.startedAt = now
        }

        // Send view updates between registers so players see the new state after "preparing" the execution of movements
        runRegister(game, board, robots, 1)
        runAutomaticSteps(game, board, robots)

        runRegister(game, board, robots, 2)
        runAutomaticSteps(game, board, robots)

        runRegister(game, board, robots, 3)
        runAutomaticSteps(game, board, robots)

        runRegister(game, board, robots, 4)
        runAutomaticSteps(game, board, robots)

        runRegister(game, board, robots, 5)
        runAutomaticSteps(game, board, robots)

        // Check for game end condition. If it is met, end the game
        if (robots.any { it.passedCheckpoints >= board.highestFlagNumber() }) {
            game.finishedAt = clock.instant()
        } else {
            // After all automatic turns are done, deal new cards so players can plan their next move
            game.state = GameState.PROGRAMMING_REGISTERS
            robots.forEach { robotEngine.prepareNewRound(game.id, it) }
        }

        database.games.update(game)
        updates.emit(ViewUpdate(game.id))
    }

    /**
     * Run all automatic steps in the game state (executed between each register)
     */
    private suspend fun runAutomaticSteps(game: Game, board: Board, robots: Collection<Robot>) {
        runMoveBoardElements(game, board, robots)
        runFireLasers(game, board, robots)
        runCheckpoints(game, board, robots)
        runRepairPowerups(game, board, robots)
    }

    /**
     * Get all movement cards of a specific register, sorted, for all players robots
     */
    private fun getRegister(gameId: Int, register: Int): List<MovementCard> {
        return database.cards
            .filter { (it.gameId eq gameId) and (it.register eq register) }
            .sortedByDescending { it.priority }
            .map { it }
    }

    /**
     * Run through all movement cards of a register in their prioritized order with delay.
     * Sends view updates after every movement.
     * Movements of more than 1 step are executed individually, with updates sent between every step
     */
    private suspend fun runRegister(game: Game, board: Board, robots: Collection<Robot>, register: Int) {
        logger.info("Running register $register of $game")
        game.state = GameState.EXECUTING_REGISTERS
        game.currentRegister = register
        database.games.update(game)
        updates.emit(ViewUpdate(game.id))
        delay(GAME_ENGINE_STEP_DELAY)

        try {
            getRegister(game.id, register)
                .forEach { card ->
                    val steps = when (card.movement) {
                        Movement.STRAIGHT_2 -> 2
                        Movement.STRAIGHT_3 -> 3
                        else -> 1
                    }

                    for (step in 1..steps) {
                        board.execute(card, robots)
                        board.flatten().forEach { database.fields.update(it) }
                        robots.forEach { database.robots.update(it) }
                        updates.emit(ViewUpdate(game.id))
                        delay(GAME_ENGINE_STEP_DELAY)
                    }
                }
        } catch (err: Throwable) {
            logger.error("Error executing register: ${err.message}", err)
        }
    }

    private suspend fun runMoveBoardElements(game: Game, board: Board, robots: Collection<Robot>) {
        game.state = GameState.MOVE_BARD_ELEMENTS_2
        database.games.update(game)

        board.moveBelts(FieldElement.BELT_2, robots)
        board.flatten().forEach { database.fields.update(it) }
        robots.forEach { database.robots.update(it) }

        updates.emit(ViewUpdate(game.id))
        delay(GAME_ENGINE_STEP_DELAY)

        game.state = GameState.MOVE_BARD_ELEMENTS_1
        database.games.update(game)

        board.moveBelts(FieldElement.BELT, robots)
        board.flatten().forEach { database.fields.update(it) }
        robots.forEach { database.robots.update(it) }

        updates.emit(ViewUpdate(game.id))
        delay(GAME_ENGINE_STEP_DELAY)
    }

    private suspend fun runFireLasers(game: Game, board: Board, robots: Collection<Robot>) {
        game.state = GameState.FIRE_LASERS_2
        database.games.update(game)

        board.fireLasers(FieldElement.LASER_2, robots)
        robots.forEach { database.robots.update(it) }

        updates.emit(ViewUpdate(game.id))
        delay(GAME_ENGINE_STEP_DELAY)

        game.state = GameState.FIRE_LASERS_1
        database.games.update(game)

        board.fireLasers(FieldElement.LASER, robots)
        robots.forEach { database.robots.update(it) }
        updates.emit(ViewUpdate(game.id))
        delay(GAME_ENGINE_STEP_DELAY)

        game.state = GameState.FIRE_ROBOT_LASERS
        database.games.update(game)

        board.fireRobotLasers(robots)
        robots.forEach { database.robots.update(it) }
        updates.emit(ViewUpdate(game.id))
        delay(GAME_ENGINE_STEP_DELAY)
    }

    private suspend fun runCheckpoints(game: Game, board: Board, robots: Collection<Robot>) {
        game.state = GameState.CHECKPOINTS
        database.games.update(game)

        board.touchCheckpoints(robots)
        robots.forEach { database.robots.update(it) }

        updates.emit(ViewUpdate(game.id))
        delay(GAME_ENGINE_STEP_DELAY)
    }

    private suspend fun runRepairPowerups(game: Game, board: Board, robots: Collection<Robot>) {
        game.state = GameState.REPAIR_POWERUPS
        database.games.update(game)

        board.touchRepair(robots)
        robots.forEach { database.robots.update(it) }
        board.touchModifications(robots)
        robots.forEach { database.robots.update(it) }

        updates.emit(ViewUpdate(game.id))
        delay(GAME_ENGINE_STEP_DELAY)
    }

    private fun removeRobot(robotId: Int) {
        database.robots.removeIf { it.id eq robotId }
    }

    private fun addRobot(gameId: Int, name: String, userId: Int, model: RobotModel): Robot {
        val robot = robotEngine.createNewRobot(gameId, name, userId, model)
        robotEngine.drawCards(gameId, robot)
        return robot
    }
}
