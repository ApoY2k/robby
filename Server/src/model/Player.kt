package apoy2k.robby.model

data class Player(val name: String, val session: Session) {
    val cards = mutableListOf<MovementCard>()
    val drawnCards = mutableListOf<MovementCard>()
    var robot: Robot? = null
}
