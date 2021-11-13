package apoy2k.robby.data

import apoy2k.robby.model.Game

interface Storage {
    fun listGames(): Collection<Game>
    fun findGame(id: String?): Game?
    fun createGame(): Game
}
