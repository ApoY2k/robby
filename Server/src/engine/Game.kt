package apoy2k.robby.engine

import apoy2k.robby.VIEW_BOARD
import apoy2k.robby.VIEW_GAME
import apoy2k.robby.VIEW_JOIN_FORM
import apoy2k.robby.VIEW_PLAYERS
import apoy2k.robby.exceptions.IncompleteCommandException
import apoy2k.robby.exceptions.InvalidGameState
import apoy2k.robby.exceptions.UnknownCommandException
import apoy2k.robby.model.*
import org.slf4j.LoggerFactory
import java.util.*

/**
 * Game engine that holds a single games' state and mutates it via commands
 */
class Game(val board: Board) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    companion object Factory {
        fun create(): Game {
            return Game(
                Board(
                    listOf(
                        listOf(Field(), Field(), Field(), Field()),
                        listOf(Field(), Field(), Field(), Field()),
                        listOf(Field(), Field(), Field(), Field()),
                        listOf(Field(), Field(), Field(), Field()),
                    )
                )
            )
        }
    }

    val players = mutableSetOf<Player>()

    /**
     * Check if a specific session has joined this game ( = is associated with a player)
     */
    fun hasJoined(session: Session?): Boolean {
        return session != null && players.any { it.session == session }
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
            logger.warn("No player with session $session found")
            return emptySet()
        }

        command.sender = player

        return when (command) {
            is LeaveGameCommand -> removePlayer(player)
            is PlaceRobotCommand -> placeRobot(command.fieldId, command.model)
            else -> throw UnknownCommandException(command)
        }
    }

    private fun removePlayer(player: Player): Set<Command> {
        players.remove(player)
        return setOf(RefreshViewCommand(VIEW_GAME))
    }

    private fun addPlayer(name: String?, session: Session): Set<Command> {
        if (name.isNullOrBlank()) {
            throw IncompleteCommandException("Player name missing")
        }

        if (players.any { it.name == name }) {
            throw InvalidGameState("Name $name is alread taken")
        }

        if (players.any { it.session == session }) {
            throw InvalidGameState("Session $session already joined")
        }

        val player = Player(name, session)
        DEFAULT_DECK.forEach { player.cards.add(it.copy()) }
        players.add(player)
        return setOf(RefreshViewCommand(VIEW_GAME, setOf(player)))
    }

    private fun placeRobot(fieldId: String?, model: String?): Set<Command> {
        if (fieldId.isNullOrBlank() || model.isNullOrBlank()) {
            throw IncompleteCommandException("Field or model missing")
        }

        board.place(UUID.fromString(fieldId), Robot.create(model))
        return setOf(RefreshViewCommand(VIEW_BOARD))
    }
}
