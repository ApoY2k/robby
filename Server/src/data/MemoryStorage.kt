package apoy2k.robby.data

import apoy2k.robby.engine.Game
import apoy2k.robby.model.Board
import apoy2k.robby.model.Field

class MemoryStorage : Storage {
    private var gameField = newGame()

    override var game: Game
        get() = gameField
        set(value) { gameField = value }
}

fun newGame(): Game {
    return Game(
        Board(
            arrayOf(
                arrayOf(Field(), Field(), Field(), Field()),
                arrayOf(Field(), Field(), Field(), Field()),
                arrayOf(Field(), Field(), Field(), Field()),
                arrayOf(Field(), Field(), Field(), Field()),
            )
        )
    )
}
