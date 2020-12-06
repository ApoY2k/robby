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

data class Robot(val model: RobotModel, val id: UUID = UUID.randomUUID()) {
    var orientation = Orientation.DOWN

    /**
     * Get an orientation in which to move this robot, based on its current orientation and a directional movement
     */
    fun getOrientationFrom(direction: Movement): Orientation {
        if (setOf(Movement.BACKWARDS, Movement.BACKWARDS_2, Movement.BACKWARDS_3).contains(direction)) {
            return when (orientation) {
                Orientation.UP -> Orientation.DOWN
                Orientation.DOWN -> Orientation.UP
                Orientation.LEFT -> Orientation.RIGHT
                Orientation.RIGHT -> Orientation.LEFT
            }
        }

        // For straight, turn or hold movements, just return the robot's orientation
        return orientation
    }
}
