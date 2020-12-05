package apoy2k.robby.engine

import apoy2k.robby.VIEW_CARDS
import apoy2k.robby.VIEW_GAME
import apoy2k.robby.VIEW_PLAYERS
import apoy2k.robby.exceptions.IncompleteCommand
import apoy2k.robby.exceptions.InvalidGameState
import apoy2k.robby.model.*
import org.slf4j.LoggerFactory

/**
 * Game engine that holds a single games' state and mutates it via commands
 */
class Game {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val players = mutableSetOf<Player>()

    val board = Board(
        listOf(
            listOf(Field(), Field(), Field(), Field()),
            listOf(Field(), Field(), Field(), Field()),
            listOf(Field(), Field(), Field(), Field()),
            listOf(Field(), Field(), Field(), Field()),
        )
    )

    val deck = mutableListOf(
        MovementCard(Movement.STRAIGHT, 1),
        MovementCard(Movement.STRAIGHT_2, 1),
        MovementCard(Movement.STRAIGHT_3, 1),
        MovementCard(Movement.BACKWARDS, 1),
        MovementCard(Movement.BACKWARDS_2, 1),
        MovementCard(Movement.BACKWARDS_3, 1),
        MovementCard(Movement.HOLD, 1),
        MovementCard(Movement.TURN_180, 1),
        MovementCard(Movement.TURN_LEFT, 1),
        MovementCard(Movement.TURN_RIGHT, 1),
    )

    // Sequence of all recorded moves for replayability
    val movements = mutableListOf<MovementCard>()
    val movementsToExecute = mutableListOf<MovementCard>()

    init {
        deck.shuffle()
    }

    /**
     * Check if a specific session has joined this game ( = is associated with a player)
     */
    fun hasJoined(session: Session?): Boolean {
        return playerFor(session) != null
    }

    /**
     * Find the player associated with a session
     */
    fun playerFor(session: Session?): Player? {
        return players.firstOrNull { it.session == session }
    }

    /**
     * Performs a set of commands to mutate the game state, in order
     * @param session Session that executes the commands
     * @return set of commands to send back to the clients for updating
     */
    fun perform(command: Command, session: Session): Set<Command> {
        if (command is JoinGameCommand) {
            return addPlayer(command.name, session)
        }

        val player = players.firstOrNull { it.session == session }
        if (player == null) {
            logger.warn("No player with session [$session] found")
            return emptySet()
        }

        command.sender = player

        var result = when (command) {
            is LeaveGameCommand -> removePlayer(player)
            is DrawCardsCommand -> drawCards(player)
            is SelectCardCommand -> {
                player.selectCard(command.cardId)
                return setOf(RefreshViewCommand(VIEW_CARDS, setOf(player)))
            }
            is ConfirmCardsCommand -> {
                player.confirmCards()
                return setOf(
                    RefreshViewCommand(VIEW_CARDS, setOf(player)),
                    RefreshViewCommand(VIEW_PLAYERS)
                )
            }
            else -> {
                logger.warn("No action mapped for [$command]")
                return emptySet()
            }
        }

        if (players.all { it.hasCardsConfirmed() }) {
            movementsToExecute.addAll(players
                .flatMap { it.getSelectedCards() }
                .filter { it.player?.robot != null }
                .sortedBy { it.priority })
        }

        return result
    }

    fun getPlayers(): Set<Player> {
        return players
    }

    private fun drawCards(player: Player): Set<Command> {
        val drawnCards = deck.take(5)
        deck.removeAll(drawnCards)
        player.takeCards(drawnCards)
        return setOf(RefreshViewCommand(VIEW_CARDS, setOf(player)))
    }

    private fun removePlayer(player: Player): Set<Command> {
        players.remove(player)

        board.cells.flatten()
            .first { it.robot == player.robot }
            .let { it.robot = null }

        return setOf(RefreshViewCommand(VIEW_GAME))
    }

    private fun addPlayer(name: String?, session: Session): Set<Command> {
        if (name.isNullOrBlank()) {
            throw IncompleteCommand("Player name missing")
        }

        if (players.any { it.name == name }) {
            throw InvalidGameState("Name [$name] is alread taken")
        }

        if (players.any { it.session == session }) {
            throw InvalidGameState("Session [$session] already joined")
        }

        val player = Player(name, session)

        val robot = Robot(RobotModel.ZIPPY)
        player.robot = robot

        players.add(player)

        board.cells.flatten()
            .first { it.robot == null }
            .let { it.robot = robot }

        return setOf(RefreshViewCommand(VIEW_GAME))
    }
}
