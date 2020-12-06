package apoy2k.robby.model

import java.util.*

enum class Movement {
    STRAIGHT,
    STRAIGHT_2,
    STRAIGHT_3,
    TURN_LEFT,
    TURN_RIGHT,
    TURN_180,
    BACKWARDS,
    BACKWARDS_2,
    BACKWARDS_3,
    HOLD,
}

data class MovementCard(val movement: Movement, val priority: Int) {
    val id: UUID = UUID.randomUUID()
    var player: Player? = null
}
