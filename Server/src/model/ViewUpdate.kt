package apoy2k.robby.model

data class ViewUpdate(val game: Game) {
    override fun toString() = "ViewUpdate(${game.id})"
}
