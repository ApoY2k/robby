package apoy2k.robby.model

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

data class MovementCard(val direction: Movement, val priority: Int) {}
