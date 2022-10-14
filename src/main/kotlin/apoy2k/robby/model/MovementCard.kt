package apoy2k.robby.model

import org.apache.commons.lang3.RandomStringUtils

enum class Movement {
    STAY,
    STRAIGHT,
    STRAIGHT_2,
    STRAIGHT_3,
    TURN_LEFT,
    TURN_RIGHT,
    TURN_180,
    BACKWARDS,
}

data class MovementCard(val movement: Movement, val priority: Int) {
    val id: String = RandomStringUtils.randomAlphanumeric(5)
    var player: Player? = null

    // true, if the movement on this card contains any amount of steps in a specific direction
    val hasSteps = setOf(
        Movement.STRAIGHT, Movement.STRAIGHT_2, Movement.STRAIGHT_3, Movement.BACKWARDS
    ).contains(movement)

    override fun toString() = "MovementCard($movement, $priority)"
}
