package apoy2k.robby.model

enum class Direction {
    UP,
    RIGHT,
    DOWN,
    LEFT,
}

/**
 * Determine the movement (turn) that resolves when moving along a curve from outside a field into a direction
 */
fun getTurnMovement(incomingDirection: Direction, outgoingDirection: Direction) =
    when (incomingDirection.ordinal - outgoingDirection.ordinal) {
        -1, 3 -> Movement.TURN_LEFT
        1, -3 -> Movement.TURN_RIGHT
        else -> Movement.STAY
    }
