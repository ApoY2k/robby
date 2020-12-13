package apoy2k.robby.engine

import apoy2k.robby.data.Storage
import apoy2k.robby.exceptions.IncompleteAction
import apoy2k.robby.exceptions.InvalidGameState
import apoy2k.robby.model.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

/**
 * Game engine to advance the game state based on commands and game rules
 */
class GameEngine(
    private val storage: Storage,
    private val actions: ReceiveChannel<Action>,
    private val updates: SendChannel<ViewUpdate>
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
                // and send back view updates that will be distributed to the clients
                perform(action).forEach { updates.send(it) }
            } catch (err: Throwable) {
                logger.error("Error executing [$action]: [$err]", err)
            }
        }
    }

    /**
     * Advances the game state by executing a single action
     */
    fun perform(action: Action): Set<ViewUpdate> {
        // All actions require a session, so if an action without a session is noticed, just drop it with
        // an empty result view update set
        val session = action.session ?: return emptySet()

        if (action is JoinGameAction) {
            return addPlayer(action.name, session)
        }

        val player = storage.game.players.firstOrNull { it.session == session }
        if (player == null) {
            logger.warn("No player with session [$session] found")
            return emptySet()
        }

        val result: MutableSet<ViewUpdate> = when (action) {
            is LeaveGameAction -> removePlayer(player)
            is SelectCardAction -> {
                player.selectCard(action.register, action.cardId)
                mutableSetOf(ViewUpdate(View.PROFILE, player))
            }
            is ConfirmCardsAction -> {
                player.toggleConfirm()
                mutableSetOf(
                    ViewUpdate(View.PROFILE, player),
                    ViewUpdate(View.PLAYERS)
                )
            }
            else -> {
                logger.warn("No action mapped for [$action]")
                mutableSetOf()
            }
        }

        if (storage.game.players.all { it.cardsConfirmed }) {
            storage.game.state = GameState.EXECUTING_REGISTER_1

            // Send view updates so players see the new state after "preparing" the execution of movements
            result.add(ViewUpdate(View.PROFILE))

            // Launch the automatic steps in a separate thread
            GlobalScope.launch {
                executeRegister(1)

                storage.game.state = GameState.EXECUTING_REGISTER_2
                executeRegister(2)

                storage.game.state = GameState.EXECUTING_REGISTER_3
                executeRegister(3)

                storage.game.state = GameState.EXECUTING_REGISTER_4
                executeRegister(4)

                storage.game.state = GameState.EXECUTING_REGISTER_5
                executeRegister(5)

                // Draw new cards for every player
                storage.game.players.forEach {
                    it.robot?.clearRegisters()
                    drawCards(it)
                    it.toggleConfirm()
                }

                storage.game.state = GameState.PROGRAMMING_REGISTERS

                // Send final updates after the moves were completed and the game state was reset
                updates.send(ViewUpdate(View.PROFILE))
            }
        }

        return result
    }

    /**
     * Execute all movement cards of a register in their prioritized order with delay.
     * Sends view updates after every movement.
     */
    private suspend fun executeRegister(register: Int) {
        try {
            storage.game.players
                .mapNotNull { it.robot?.getRegister(register) }
                .sortedByDescending { it.priority }
                .forEach {
                    storage.game.board.execute(it)
                    updates.send(ViewUpdate(View.BOARD))
                    updates.send(ViewUpdate(View.PROFILE))

                    delay(1000)
                }
        } catch (err: Throwable) {
            logger.error("Error executing register: [${err.message}]", err)
        }
    }

    private fun drawCards(player: Player): MutableSet<ViewUpdate> {
        val robot = player.robot ?: return mutableSetOf()

        val drawnCards = storage.game.deck.take(9 - robot.damage)
        storage.game.deck.removeAll(drawnCards)
        player.takeCards(drawnCards)
        return mutableSetOf(ViewUpdate(View.PROFILE, player))
    }

    private fun removePlayer(player: Player): MutableSet<ViewUpdate> {
        storage.game.players.remove(player)

        storage.game.board.fields.flatten()
            .firstOrNull { it.robot == player.robot }
            .let { it?.robot = null }

        return mutableSetOf(ViewUpdate(View.PLAYERS), ViewUpdate(View.BOARD))
    }

    private fun addPlayer(name: String?, session: Session): MutableSet<ViewUpdate> {
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

        return mutableSetOf(ViewUpdate(View.PLAYERS), ViewUpdate(View.BOARD), ViewUpdate(View.PROFILE))
    }
}
