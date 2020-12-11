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
     * Stores all movement cards that have to be executed once all players confirm their movements
     */
    var movementsToExecute = listOf<MovementCard>()
        private set

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
                player.selectCard(action.cardId)
                mutableSetOf(ViewUpdate(View.CARDS, player))
            }
            is ConfirmCardsAction -> {
                player.toggleConfirm()
                mutableSetOf(
                    ViewUpdate(View.CARDS, player),
                    ViewUpdate(View.PLAYERS)
                )
            }
            else -> {
                logger.warn("No action mapped for [$action]")
                mutableSetOf()
            }
        }

        if (storage.game.players.all { it.cardsConfirmed }) {

            // If all players have confirmed their cards, add them to the movements
            // to execute list in order. The scheduler will later execute each movement
            // and notify all attached clients of the resulting game state
            movementsToExecute = storage.game.players
                .flatMap { it.selectedCards }
                .filter { it.player?.robot != null }
                .sortedByDescending { it.priority }

            // As their movement cards are now moved, remove them from all players and give each player an empty hand
            storage.game.players.forEach {
                it.selectedCards.clear()
                it.takeCards(emptyList())
                it.toggleConfirm()
            }

            // Send view updates so players see the new state after "preparing" the execution of movements
            result.add(ViewUpdate(View.CARDS))


            // Execute the current set of movements in a separate ocroutine and send view updates to
            // the update channel. Removes the movements one by one after they were executed
            GlobalScope.launch {
                executeMovements()
            }
        }

        return result
    }

    /**
     * Execute all movements with a delay in between, sending view updates on every step.
     * Also sends final view updates after the movements were executed.
     */
    private suspend fun executeMovements() {
        while (movementsToExecute.isNotEmpty()) {
            delay(1000)

            val nextMovement = movementsToExecute.first()
            try {
                storage.game.board.execute(nextMovement)
                updates.send(ViewUpdate(View.BOARD))
            } catch (err: Throwable) {
                logger.error("Error executing movement: [${err.message}", err)
            } finally {
                movementsToExecute = movementsToExecute.drop(1)
            }
        }

        // After all movements were executed, set the game state back so players can interact again
        storage.game.players.forEach {
            drawCards(it)
        }

        // Send final updates after the moves were completed and the game state was reset
        updates.send(ViewUpdate(View.CARDS))
    }

    private fun drawCards(player: Player): MutableSet<ViewUpdate> {
        val drawnCards = storage.game.deck.take(5)
        //storage.game.deck.removeAll(drawnCards)
        storage.game.deck.shuffle()
        player.takeCards(drawnCards)
        return mutableSetOf(ViewUpdate(View.CARDS, player))
    }

    private fun removePlayer(player: Player): MutableSet<ViewUpdate> {
        storage.game.players.remove(player)

        storage.game.board.fields.flatten()
            .firstOrNull { it.robot == player.robot }
            .let { it?.robot = null }

        return mutableSetOf(ViewUpdate(View.GAME))
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

        return mutableSetOf(ViewUpdate(View.GAME))
    }
}
