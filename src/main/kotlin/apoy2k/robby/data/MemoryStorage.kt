package apoy2k.robby.data

import apoy2k.robby.model.Game

class MemoryStorage : Storage {
    private val games = mutableListOf<Game>()
    override fun listGames() = games.toMutableList()
    override fun findGame(id: String) = games.firstOrNull { it.id == id }
    override fun createGame(): Game {
        val game = Game()
        games.add(game)
        return game
    }
}
