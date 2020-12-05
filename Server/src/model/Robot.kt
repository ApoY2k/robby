package apoy2k.robby.model

import java.util.*

enum class RobotModel {
    ZIPPY,
    GEROG,
    KLAUS,
    HUZZA,
}

enum class Orientation {
    UP,
    RIGHT,
    DOWN,
    LEFT
}

/**
 * Get an orientation in which to move a robot, based on its current orientation and a directional movement
 */
fun Robot.getOrientationFrom(direction: Movement): Orientation {
    if (setOf(Movement.BACKWARDS, Movement.BACKWARDS_2, Movement.BACKWARDS_3).contains(direction)) {
        return when (this.orientation) {
            Orientation.UP -> Orientation.DOWN
            Orientation.DOWN -> Orientation.UP
            Orientation.LEFT -> Orientation.RIGHT
            Orientation.RIGHT -> Orientation.LEFT
        }
    }

    // For straight, turn or hold movements, just return the robot's orientation
    return this.orientation
}

data class Robot(val model: RobotModel, val id: UUID = UUID.randomUUID()) {
    var orientation = Orientation.DOWN
}
