package apoy2k.robby.model

import kotlinx.html.Dir
import java.util.*

enum class RobotModel {
    ZIPPY,
    GEROG,
    KLAUS,
    HUZZA,
}

enum class Direction {
    UP,
    RIGHT,
    DOWN,
    LEFT
}

data class Robot(val model: RobotModel, val id: UUID = UUID.randomUUID()) {
    var facing = Direction.DOWN

    /**
     * Apply a rotational movement on this robot
     */
    fun rotate(movement: Movement) {
        if (movement == Movement.TURN_RIGHT) {
            facing = when(facing) {
                Direction.UP -> Direction.RIGHT
                Direction.DOWN -> Direction.LEFT
                Direction.LEFT -> Direction.UP
                Direction.RIGHT -> Direction.DOWN
            }
        }

        if (movement == Movement.TURN_LEFT) {
            facing = when(facing) {
                Direction.UP -> Direction.LEFT
                Direction.DOWN -> Direction.RIGHT
                Direction.LEFT -> Direction.DOWN
                Direction.RIGHT -> Direction.UP
            }
        }

        if (movement == Movement.TURN_180) {
            facing = when(facing) {
                Direction.UP -> Direction.DOWN
                Direction.DOWN -> Direction.UP
                Direction.LEFT -> Direction.RIGHT
                Direction.RIGHT -> Direction.LEFT
            }
        }

        // Any other movement don't rotate the robot and can be ignored
    }

    /**
     * Get the direction in which to move this robot, based on where its facind and a movement to apply
     */
    fun getMovementDirection(movement: Movement): Direction {
        if (setOf(Movement.BACKWARDS, Movement.BACKWARDS_2, Movement.BACKWARDS_3).contains(movement)) {
            return when (facing) {
                Direction.UP -> Direction.DOWN
                Direction.DOWN -> Direction.UP
                Direction.LEFT -> Direction.RIGHT
                Direction.RIGHT -> Direction.LEFT
            }
        }

        // Any other movement do result in an actual movement. For those cases, return the direction the robot
        // is facing so it will move (or stay) in that direction
        return facing
    }
}
