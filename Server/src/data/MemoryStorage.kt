package apoy2k.robby.data

import apoy2k.robby.model.Game

class MemoryStorage : Storage {
    private var _game = Game()

    override var game: Game
        get() = _game
        set(value) { _game = value }
}
