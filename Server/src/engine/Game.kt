package apoy2k.robby.engine

import apoy2k.robby.exceptions.UnknownCommandException
import apoy2k.robby.model.*
import java.util.*

/**
 * Represents all information about a single game being played
 */
class Game(val board: Board) {
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
     * Performs a set of commands to mutate the game state, in order
     * @return set of commands to send back to the clients for updating
     */
    fun perform(commands: List<Command>): Set<Command> {
        return commands.flatMap {
            when (it) {
                is JoinGameCommand -> addPlayer(it.name)
                is LeaveGameCommand -> removePlayer(it.name)
                is PlaceRobotCommand -> placeRobot(it.fieldId, it.model)
                else -> throw UnknownCommandException(it)
            }
        }.toSet()
    }

    private fun removePlayer(name: String): Set<Command> {
        players.remove(Player(name))
        return setOf(RefreshPlayersCommand())
    }

    private fun addPlayer(name: String): Set<Command> {
        val player = Player(name)
        DEFAULT_DECK.forEach { player.cards.add(it.copy()) }
        players.add(player)
        return setOf(RefreshPlayersCommand())
    }

    private fun placeRobot(fieldId: String, model: String): Set<Command> {
        board.place(UUID.fromString(fieldId), Robot.create(model))
        return setOf(RefreshBoardCommand())
    }
}
