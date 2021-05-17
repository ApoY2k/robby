package apoy2k.robby.engine

import apoy2k.robby.data.Storage
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
 * Game engine to advance the game state based on commands and game rules
 */
class GameEngine(
    private val storage: Storage,
    private val actions: ReceiveChannel<Action>,
    private val updates: SendChannel<Unit>
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Starts listening to message on the command channel and executes them, sending back
     * commands to be routed to the players
     */
    @ExperimentalCoroutinesApi
    suspend fun connect() {
        actions.consumeEach { action ->
            try {
                logger.debug("Received [$action]")

                // For each incoming action, perform the action on the game state
                perform(action)
                updates.send(Unit)

                // If after the action was performed, all players have their cards confirmed,
                // execut the registers and send updates accordingly
                if (storage.game.players.isNotEmpty() && storage.game.players.all { it.cardsConfirmed }) {

                    // Send view updates so players see the new state after "preparing" the execution of movements
                    storage.game.state = GameState.EXECUTING_REGISTER_1
                    updates.send(Unit)
                    runRegister(1)
                    storage.game.state = GameState.EXECUTING_REGISTER_2
                    runRegister(2)
                    storage.game.state = GameState.EXECUTING_REGISTER_3
                    runRegister(3)
                    storage.game.state = GameState.EXECUTING_REGISTER_4
                    runRegister(4)
                    storage.game.state = GameState.EXECUTING_REGISTER_5
                    runRegister(5)

                    // After movements, execute all other game states automatically in order
                    // TODO All of this should happen between each register??

                    storage.game.state = GameState.MOVE_BARD_ELEMENTS
                    runMoveBoardElements()

                    storage.game.state = GameState.FIRE_LASERS
                    runFireLasers()

                    storage.game.state = GameState.CHECKPOINTS
                    runCheckpoints()

                    storage.game.state = GameState.REPAIR_POWERUPS
                    runRepairPowerups()

                    // After all automatic turns are done, deal new cards so players can plan their next move

                    storage.game.state = GameState.PROGRAMMING_REGISTERS
                    storage.game.players.forEach {
                        it.robot?.clearRegisters()
                        drawCards(it)
                        it.toggleConfirm()
                    }
                    updates.send(Unit)
                }
            } catch (err: Throwable) {
                logger.error("Error executing [$action]: [$err]", err)
            }
        }
    }

    /**
     * Advances the game state by executing a single action.
     * Returns a set of ViewUpdates to send to the clients.
     */
    fun perform(action: Action) {
        // All actions require a session, so if an action without a session is noticed, just drop it with
        // an empty result view update set
        val session = action.session ?: return

        if (action is JoinGameAction) {
            addPlayer(action.name, session)
            return
        }

        val player = storage.game.players.firstOrNull { it.session == session }
        if (player == null) {
            logger.warn("No player with session [$session] found")
            return
        }

        when (action) {
            is LeaveGameAction -> removePlayer(player)
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
    fun getRegister(register: Int): List<MovementCard> {
        return storage.game.players
            .mapNotNull { it.robot?.getRegister(register) }
            .sortedByDescending { it.priority }
    }

    /**
     * Execute all movement cards of a register in their prioritized order.
     */
    fun executeRegister(register: Int) {
        try {
            getRegister(register)
                .forEach {
                    storage.game.board.execute(it)
                }
        } catch (err: Throwable) {
            logger.error("Error executing register: [${err.message}]", err)
        }
    }

    /**
     * Run through all movement cards of a register in their prioritized order with delay.
     * Sends view updates after every movement.
     * Movements of more than 1 step are executed individually, with updates sent between every step
     */
    private suspend fun runRegister(register: Int) {
        try {
            getRegister(register)
                .forEach {
                    val steps = when(it.movement) {
                        Movement.STRAIGHT_2 -> 2
                        Movement.STRAIGHT_3 -> 3
                        else -> 1
                    }

                    for (step in 1..steps) {
                        storage.game.board.execute(it)
                        updates.send(Unit)
                        delay(1000)
                    }
                }
        } catch (err: Throwable) {
            logger.error("Error executing register: [${err.message}]", err)
        }
    }

    private suspend fun runMoveBoardElements() {
        storage.game.board.moveBelts(FieldType.BELT_2)
        updates.send(Unit)
        delay(1000)

        // TODO Move BELT_2 two times?

        storage.game.board.moveBelts(FieldType.BELT)
        updates.send(Unit)
        delay(1000)
    }

    private suspend fun runFireLasers() {
        storage.game.board.fireLasers(FieldType.LASER_2)
        updates.send(Unit)
        delay(1000)

        // TODO Fire LASER_2 two times?

        storage.game.board.fireLasers(FieldType.LASER)
        updates.send(Unit)
        delay(1000)
    }

    private suspend fun runCheckpoints() {
        // TODO Implement checkpoints
        /**
         * For each robot, check the field type and if it is a checkpoint
         * If yes, mark the players checkpoints accordingly
         */
        updates.send(Unit)
        delay(1000)
    }

    private suspend fun runRepairPowerups() {
        storage.game.board.fields.flatten()
            .filter { it.robot != null }
            .filter { it.type == FieldType.REPAIR }
            .forEach { it.robot?.let { robot ->
                robot.damage = max(0, robot.damage - 1)
            } }
        updates.send(Unit)
        delay(1000)
    }

    private fun drawCards(player: Player) {
        val robot = player.robot ?: return

        val drawnCards = storage.game.deck.take(9 - robot.damage)
        storage.game.deck.removeAll(drawnCards)
        player.takeCards(drawnCards)
    }

    private fun removePlayer(player: Player) {
        storage.game.players.remove(player)

        storage.game.board.fields.flatten()
            .firstOrNull { it.robot == player.robot }
            .let { it?.robot = null }
    }

    private fun addPlayer(name: String?, session: Session) {
        if (name.isNullOrBlank()) {
            throw IncompleteAction("Player name missing")
        }

        if (storage.game.players.any { it.name == name }) {
            throw InvalidGameState("Name [$name] is alread taken")
        }

        if (storage.game.players.any { it.session == session }) {
            throw InvalidGameState("Session [$session] already joined")
        }

        val player = Player(name, session)

        val robot = Robot(RobotModel.ZIPPY)
        player.robot = robot

        storage.game.players.add(player)

        drawCards(player)

        storage.game.board.fields.flatten()
            .first { it.robot == null }
            .let { it.robot = robot }
    }
}
