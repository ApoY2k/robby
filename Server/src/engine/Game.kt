package apoy2k.robby.engine

import apoy2k.robby.VIEW_BOARD
import apoy2k.robby.VIEW_CARDS
import apoy2k.robby.VIEW_GAME
import apoy2k.robby.VIEW_PLAYERS
import apoy2k.robby.exceptions.IncompleteCommand
import apoy2k.robby.exceptions.InvalidGameState
import apoy2k.robby.exceptions.UnknownCommand
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
            logger.warn("No player with session $session found")
            return emptySet()
        }

        command.sender = player

        var result = when (command) {
            is LeaveGameCommand -> removePlayer(player)
            is PlaceRobotCommand -> placeRobot(player, command.fieldId, command.model)
            is DrawCardsCommand -> {
                player.drawCards()
                return setOf(RefreshViewCommand(VIEW_CARDS, setOf(player)))
            }
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

        if (players.all { it.cardsConfirmed }) {
            // TODO : Trigger execution of commands
        }

        return result
    }

    private fun removePlayer(player: Player): Set<Command> {
        players.remove(player)
        return setOf(RefreshViewCommand(VIEW_GAME))
    }

    private fun addPlayer(name: String?, session: Session): Set<Command> {
        if (name.isNullOrBlank()) {
            throw IncompleteCommand("Player name missing")
        }

        if (players.any { it.name == name }) {
            throw InvalidGameState("Name $name is alread taken")
        }

        if (players.any { it.session == session }) {
            throw InvalidGameState("Session $session already joined")
        }

        val player = Player(name, session)
        DEFAULT_DECK.forEach { player.drawPile.add(it.copy()) }
        players.add(player)
        return setOf(RefreshViewCommand(VIEW_GAME, setOf(player)))
    }

    private fun placeRobot(player: Player, fieldId: String?, model: String?): Set<Command> {
        if (fieldId.isNullOrBlank() || model.isNullOrBlank()) {
            throw IncompleteCommand("Field or model missing")
        }

        val robot = Robot.create(model)
        player.robot = robot
        board.place(UUID.fromString(fieldId), robot)
        return setOf(RefreshViewCommand(VIEW_BOARD))
    }
}
