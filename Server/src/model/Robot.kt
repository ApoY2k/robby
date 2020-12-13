package apoy2k.robby.model

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

    private var registers = mutableMapOf<Int, MovementCard?>(
        1 to null,
        2 to null,
        3 to null,
        4 to null,
        5 to null,
    )

    val damage = 0

    val poweredDown = false

    val powerUps = emptyList<PowerUpCard>()

    /**
     * Move a card into a register
     */
    fun setRegister(register: Int, card: MovementCard) {
        // Remove the card from any other register, if it's already registered somewhere else
        // as each card can only be registered once
        registers
            .filter { it.value == card }
            .forEach { registers[it.key] = null }

        // Then register the card in the given register
        registers[register] = card
    }

    /**
     * Clear all registers
     */
    fun clearRegisters() {
        registers = mutableMapOf()
    }

    /**
     * Returns true if the robot has movement cards in all registers
     */
    fun hasAllRegistersFilled(): Boolean {
        return registers.all { it.value != null }
    }

    /**
     * Return the movement card in a register (or null if no card is set)
     */
    fun getRegister(register: Int): MovementCard? {
        return registers[register]
    }

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
        if (setOf(Movement.BACKWARDS).contains(movement)) {
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
