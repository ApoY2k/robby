package apoy2k.robby.engine

import apoy2k.robby.exceptions.IncompleteAction
import apoy2k.robby.exceptions.InvalidGameState
import apoy2k.robby.model.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.lang.Integer.max

private const val GAME_ENGINE_STEP_DELAY = 1000L

/**
 * Game engine to advance games based on incoming actions
 */
class GameEngine(private val updates: MutableSharedFlow<ViewUpdate>) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Starts listening to message on the command channel and executes them on the corresponding game,
     * sending back viewupdates for the game along the way
     */
    suspend fun connect(actions: SharedFlow<Action>) = coroutineScope {
        actions.onEach { action ->
            val game = action.game
            if (game == null) {
                logger.warn("Could not find game corresponding to $action")
                return@onEach
            }

            if (game.isFinished) {
                logger.warn("$game is already finished! Ignorning $action")
                return@onEach
            }

            try {
                logger.debug("Received $action")

                // Perform the action and run automatic registers and actions
                // with ViewUpdates in a separate coroutine so the channel isn't blocked
                launch {
                    perform(action)
                    updates.emit(ViewUpdate(game))
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

        if (action is JoinGameAction) {
            addPlayer(game, session.name, RobotModel.valueOf(action.model ?: "ZIPPY"), session)
            return
        }

        val player = game.players.firstOrNull { it.session == session }
        if (player == null) {
            logger.warn("No player found for $session")
            return
        }

        when (action) {
            is LeaveGameAction -> removePlayer(game, player)
            is SelectCardAction -> {
                player.selectCard(action.register, action.cardId)
            }

            is ToggleReady -> {
                player.toggleReady()
            }

            is TogglePowerDown -> {
                player.togglePowerDown()
            }

            else -> {
                logger.warn("No executor for $action")
            }
        }
    }

    /**
     * Run all automatic steps in the game state (executed between each register)
     */
    private suspend fun runAutomaticSteps(game: Game) {
        game.state = GameState.MOVE_BARD_ELEMENTS
        runMoveBoardElements(game)

        game.state = GameState.FIRE_LASERS
        runFireLasers(game)

        game.state = GameState.CHECKPOINTS
        runCheckpoints(game)

        game.state = GameState.REPAIR_POWERUPS
        runRepairPowerups(game)
    }

    /**
     * Run engine on game state, sends view updates accordingly
     */
    private suspend fun runEngine(game: Game) {

        // Check conditions for running automatic registers are fulfilled
        if (game.players.isEmpty() || game.players.any { !it.ready }) {
            return
        }

        game.hasStarted = true

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
        if (game.players.any { it.robot.passedCheckpoints == 3 }) {
            game.isFinished = true
        } else {
            // After all automatic turns are done, deal new cards so players can plan their next move
            game.state = GameState.PROGRAMMING_REGISTERS
            game.players.forEach {
                it.robot.clearRegisters()
                it.toggleReady()

                if (it.powerDownScheduled) {
                    it.togglePowerDown()
                    it.robot.poweredDown = true
                } else {
                    drawCards(game, it)

                    with(it.robot) {
                        if (poweredDown) {
                            damage = 0
                            poweredDown = false
                        }
                    }
                }
            }
        }

        updates.emit(ViewUpdate(game))
    }

    /**
     * Get all movement cards of a specific register, sorted, for all players robots
     */
    private fun getRegister(game: Game, register: Int): List<MovementCard> {
        return game.players
            .mapNotNull { it.robot.getRegister(register) }
            .sortedByDescending { it.priority }
    }

    /**
     * Run through all movement cards of a register in their prioritized order with delay.
     * Sends view updates after every movement.
     * Movements of more than 1 step are executed individually, with updates sent between every step
     */
    private suspend fun runRegister(game: Game, register: Int) {
        game.state = GameState.EXECUTING_REGISTER
        game.currentRegister = register
        updates.emit(ViewUpdate(game))

        try {
            getRegister(game, register)
                .forEach {
                    val steps = when (it.movement) {
                        Movement.STRAIGHT_2 -> 2
                        Movement.STRAIGHT_3 -> 3
                        else -> 1
                    }

                    for (step in 1..steps) {
                        game.board.execute(it)
                        updates.emit(ViewUpdate(game))
                        delay(GAME_ENGINE_STEP_DELAY)
                    }
                }
        } catch (err: Throwable) {
            logger.error("Error executing register: ${err.message}", err)
        }
    }

    private suspend fun runMoveBoardElements(game: Game) {
        game.board.moveBelts(FieldType.BELT_2)
        updates.emit(ViewUpdate(game))
        delay(GAME_ENGINE_STEP_DELAY)

        game.board.moveBelts(FieldType.BELT)
        updates.emit(ViewUpdate(game))
        delay(GAME_ENGINE_STEP_DELAY)
    }

    private suspend fun runFireLasers(game: Game) {
        game.board.fireLasers(FieldType.LASER_2)
        updates.emit(ViewUpdate(game))
        delay(GAME_ENGINE_STEP_DELAY)

        game.board.fireLasers(FieldType.LASER)
        updates.emit(ViewUpdate(game))
        delay(GAME_ENGINE_STEP_DELAY)
    }

    private suspend fun runCheckpoints(game: Game) {
        game.board.fields.flatten()
            .filter { it.type == FieldType.FLAG }
            .mapNotNull { it.robot }
            .forEach { it.passedCheckpoints += 1 }
        updates.emit(ViewUpdate(game))
        delay(GAME_ENGINE_STEP_DELAY)
    }

    private suspend fun runRepairPowerups(game: Game) {
        game.board.fields.flatten()
            .filter { it.type == FieldType.REPAIR }
            .mapNotNull { it.robot }
            .forEach { it.damage = max(0, it.damage - 1) }

        // TODO Implement powerups

        updates.emit(ViewUpdate(game))
        delay(GAME_ENGINE_STEP_DELAY)
    }

    private fun drawCards(game: Game, player: Player) {
        val robot = player.robot
        val drawnCards = game.deck.take(max(0, 9 - robot.damage))
        game.deck.removeAll(drawnCards)
        player.takeCards(drawnCards)
    }

    private fun removePlayer(game: Game, player: Player) {
        game.players.remove(player)

        game.board.fields.flatten()
            .firstOrNull { it.robot == player.robot }
            .let { it?.robot = null }
    }

    private fun addPlayer(game: Game, name: String, model: RobotModel, session: Session) {
        if (name.isBlank()) {
            throw IncompleteAction("No name provided")
        }
        if (game.players.any { it.session == session }) {
            throw InvalidGameState("$session already joined")
        }

        val robot = Robot(model)
        val player = Player(name, robot, session)

        game.players.add(player)

        drawCards(game, player)

        game.board.fields.flatten()
            .first { it.robot == null }
            .let { it.robot = robot }
    }
}
