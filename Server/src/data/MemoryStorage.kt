package apoy2k.robby.data

import apoy2k.robby.engine.Game

class MemoryStorage : Storage {
    private var gameField = Game.create()

    override var game: Game
        get() = gameField
        set(value) { gameField = value }
}
