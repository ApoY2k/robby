package apoy2k.robby.engine

import apoy2k.robby.data.Storage
import apoy2k.robby.exceptions.IncompleteAction
import apoy2k.robby.exceptions.InvalidGameState
import apoy2k.robby.model.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors

/**
 * Game engine to advance the game state based on commands and game rules
 */
class GameEngine(private val storage: Storage) {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val executorService = Executors.newFixedThreadPool(1)

    /**
     * Stores all movement cards that have to be executed once all players confirm their movements
     */
    private val movementsToExecute = mutableListOf<MovementCard>()

    /**
     * Starts listening to message on the command channel and executes them, sending back
     * commands to be routed to the players
     */
    fun connect(actions: ReceiveChannel<Action>, updates: SendChannel<ViewUpdate>) {
        executorService.submit {
            runBlocking {
                for (action in actions) {
                    logger.debug("Received [$action]")

                    // Catch the ExecuteMovement action as it's an entirely internal action
                    // and can be executed without any associated session. It also always
                    // returns a ViewUpdate for the board which is broadcast to all players
                    if (action is ExecuteMovementAction) {
                        executeNextMovement().also { updates.send(it) }
                        continue
                    }

                    // For each incoming action, perform the action on the game state
                    // and send back view updates that will be distributed to the clients
                    perform(action)
                        .forEach { updates.send(it) }
                }
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
            is DrawCardsAction -> drawCards(player)
            is SelectCardAction -> {
                player.selectCard(action.cardId)
                mutableSetOf(ViewUpdate(View.CARDS, player))
            }
            is ConfirmCardsAction -> {
                player.confirmCards()
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
            movementsToExecute.addAll(storage.game.players
                .flatMap { it.selectedCards }
                .filter { it.player?.robot != null }
                .sortedBy { it.priority })

            // As their movement cards are now moved, remove them from all players and draw each player a new hand
            // which will also add new refresh commands to the players so they see their new hand
            storage.game.players.forEach {
                it.selectedCards.clear()
                result.addAll(drawCards(it))
            }
        }

        return result
    }

    /**
     * Execute the next movement of the current list of movement cards to be executed (if any).
     *
     */
    fun executeNextMovement(): ViewUpdate {
        val nextMovement = movementsToExecute.removeAt(0)
        storage.game.board.execute(nextMovement)
        return ViewUpdate(View.BOARD)
    }

    private fun drawCards(player: Player): MutableSet<ViewUpdate> {
        val drawnCards = storage.game.deck.take(5)
        storage.game.deck.removeAll(drawnCards)
        player.takeCards(drawnCards)
        return mutableSetOf(ViewUpdate(View.CARDS, player))
    }

    private fun removePlayer(player: Player): MutableSet<ViewUpdate> {
        storage.game.players.remove(player)

        storage.game.board.fields.flatten()
            .first { it.robot == player.robot }
            .let { it.robot = null }

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

        storage.game.board.fields.flatten()
            .first { it.robot == null }
            .let { it.robot = robot }

        return mutableSetOf(ViewUpdate(View.GAME))
    }
}
