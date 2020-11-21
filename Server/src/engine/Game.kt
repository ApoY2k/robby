package apoy2k.robby.engine

import apoy2k.robby.exceptions.UnknownCommandException
import apoy2k.robby.model.Board
import apoy2k.robby.model.Field

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

    /**
     * Performs a set of commands to mutate the game state, in order
     * @return set of commands to send back to the clients for updating
     */
    fun perform(commands: List<Command>): Set<Command> {
        return commands.flatMap {
            when (it) {
                is SwitchFieldCommand -> {
                    board.flip(it.id)
                    setOf(RefreshBoardCommand())
                }
                else -> throw UnknownCommandException(it)
            }
        }.toSet()
    }
}
