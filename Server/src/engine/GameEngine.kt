package apoy2k.robby.engine

import apoy2k.robby.exceptions.IncompleteAction
import apoy2k.robby.exceptions.InvalidGameState
import apoy2k.robby.model.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import java.lang.Integer.max

/**
 * Game engine to advance games based on incoming actions
 */
@ExperimentalCoroutinesApi
class GameEngine(private val updates: SendChannel<ViewUpdate>) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Starts listening to message on the command channel and executes them on the corresponding game,
     * sending back viewupdates for the game along the way
     */
    // TODO: When two games are running in parallel, the engine ony works on one game at a time
    // TODO: Also, creating two games in parallel somehow overlaps the joined players?
    suspend fun connect(actions: ReceiveChannel<Action>) {
        actions.consumeEach { action ->
            val game = action.game
            if (game == null) {
                logger.warn("Could not find game corresponding to $action")
                return@consumeEach
            }

            if (game.state == GameState.FINISHED) {
                logger.warn("$game is already finished! Ignorning $action")
                return@consumeEach
            }

            try {
                logger.debug("Received $action")

                // For each incoming action, perform the action on the game state
                perform(action)
                updates.send(ViewUpdate(game))

                // If after the action was performed, all players have their cards confirmed,
                // execut the registers and send updates accordingly
                if (game.players.isNotEmpty() && game.players.all { it.cardsConfirmed }) {

                    // Send view updates so players see the new state after "preparing" the execution of movements
                    game.state = GameState.EXECUTING_REGISTER_1
                    updates.send(ViewUpdate(game))
                    runRegister(game, 1)
                    runAutomaticSteps(game)

                    game.state = GameState.EXECUTING_REGISTER_2
                    runRegister(game, 2)
                    runAutomaticSteps(game)

                    game.state = GameState.EXECUTING_REGISTER_3
                    runRegister(game, 3)
                    runAutomaticSteps(game)

                    game.state = GameState.EXECUTING_REGISTER_4
                    runRegister(game, 4)
                    runAutomaticSteps(game)

                    game.state = GameState.EXECUTING_REGISTER_5
                    runRegister(game, 5)
                    runAutomaticSteps(game)

                    // Check for end condition. If it is met, end the game
                    if (game.players.any { it.robot?.passedCheckpoints == 3 }) {
                        game.state = GameState.FINISHED
                    } else {
                        // After all automatic turns are done, deal new cards so players can plan their next move
                        game.state = GameState.PROGRAMMING_REGISTERS
                        game.players.forEach {
                            it.robot?.clearRegisters()
                            drawCards(game, it)
                            it.toggleConfirm()
                        }
                    }

                    updates.send(ViewUpdate(game))
                }
            } catch (err: Throwable) {
                logger.error("Error executing [$action]: [$err]", err)
            }
        }
    }

    // Run all automatic steps in the game state (executed between each register)
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
     * Advances the game state by executing a single action.
     * Returns a set of ViewUpdates to send to the clients.
     */
    fun perform(action: Action) {
        // All actions require a session, so if an action without a session is noticed, just drop it with
        // an empty result view update set
        val session = action.session ?: return
        val game = action.game ?: throw IncompleteAction("No game attached to $action")

        if (action is JoinGameAction) {
            addPlayer(game, session.name, RobotModel.valueOf(action.model ?: "ZIPPY"), session)
            return
        }

        val player = game.players.firstOrNull { it.session == session }
        if (player == null) {
            logger.warn("No player with session [$session] found")
            return
        }

        when (action) {
            is LeaveGameAction -> removePlayer(game, player)
            is SelectCardAction -> {
                player.selectCard(action.register, action.cardId)
            }
            is ConfirmCardsAction -> {
                player.toggleConfirm()
            }
            else -> {
                logger.warn("No action mapped for [$action]")
            }
        }
    }

    /**
     * Get all movement cards of a specific register, sorted, for all players robots
     */
    private fun getRegister(game: Game, register: Int): List<MovementCard> {
        return game.players
            .mapNotNull { it.robot?.getRegister(register) }
            .sortedByDescending { it.priority }
    }

    /**
     * Run through all movement cards of a register in their prioritized order with delay.
     * Sends view updates after every movement.
     * Movements of more than 1 step are executed individually, with updates sent between every step
     */
    private suspend fun runRegister(game: Game, register: Int) {
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
                        updates.send(ViewUpdate(game))
                        delay(1000)
                    }
                }
        } catch (err: Throwable) {
            logger.error("Error executing register: [${err.message}]", err)
        }
    }

    private suspend fun runMoveBoardElements(game: Game) {
        game.board.moveBelts(FieldType.BELT_2)
        updates.send(ViewUpdate(game))
        delay(1000)

        game.board.moveBelts(FieldType.BELT)
        updates.send(ViewUpdate(game))
        delay(1000)
    }

    private suspend fun runFireLasers(game: Game) {
        game.board.fireLasers(FieldType.LASER_2)
        updates.send(ViewUpdate(game))
        delay(1000)

        // Remove the laser conditions on fields after they fired
        game.board.fields.flatten().forEach { it.conditions.remove(FieldCondition.LASER) }
        updates.send(ViewUpdate(game))
        delay(1000)

        game.board.fireLasers(FieldType.LASER)
        updates.send(ViewUpdate(game))
        delay(1000)

        // Remove the laser conditions on fields after they fired
        game.board.fields.flatten().forEach { it.conditions.remove(FieldCondition.LASER_2) }
        updates.send(ViewUpdate(game))
        delay(1000)
    }

    private suspend fun runCheckpoints(game: Game) {
        game.board.fields.flatten()
            .filter { it.robot != null }
            .forEach {
                it.robot?.let { robot ->
                    robot.passedCheckpoints += 1
                }
            }
        updates.send(ViewUpdate(game))
        delay(1000)
    }

    private suspend fun runRepairPowerups(game: Game) {
        game.board.fields.flatten()
            .filter { it.robot != null }
            .filter { it.type == FieldType.REPAIR }
            .forEach {
                it.robot?.let { robot ->
                    robot.damage = max(0, robot.damage - 1)
                }
            }

        // TODO Implement powerups

        updates.send(ViewUpdate(game))
        delay(1000)
    }

    private fun drawCards(game: Game, player: Player) {
        val robot = player.robot ?: return

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

    private fun addPlayer(game: Game, name: String?, model: RobotModel, session: Session) {
        if (name.isNullOrBlank()) {
            throw IncompleteAction("Player name missing")
        }

        if (game.players.any { it.session == session }) {
            throw InvalidGameState("Session [$session] already joined")
        }

        val player = Player(name, session)

        val robot = Robot(model)
        player.robot = robot

        game.players.add(player)

        drawCards(game, player)

        game.board.fields.flatten()
            .first { it.robot == null }
            .let { it.robot = robot }
    }
}
