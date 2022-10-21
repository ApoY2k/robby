package apoy2k.robby.model

import org.apache.commons.lang3.RandomStringUtils
import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*

enum class RobotModel {
    ZIPPY,
    GEROG,
    KLAUS,
    HUZZA,
}

enum class Direction {
    NONE,
    UP,
    RIGHT,
    DOWN,
    LEFT;

    fun isVertical() = this == UP || this == DOWN
    fun isHorizontal() = this == LEFT || this == RIGHT
}

data class Vec2(val x: Float, val y: Float)

fun Direction.toVec2() = when (this) {
    Direction.NONE -> Vec2(0f, 0f)
    Direction.UP -> Vec2(0f, 1f)
    Direction.RIGHT -> Vec2(1f, 0f)
    Direction.DOWN -> Vec2(0f, -1f)
    Direction.LEFT -> Vec2(-1f, 0f)
}

interface DbRobot : Entity<DbRobot> {
    companion object : Entity.Factory<DbRobot>()

    val id: Long
    var game: DbGame
    var model: RobotModel
    var facing: Direction
    var damage: Short
    var poweredDown: Boolean
    var passedCheckpoints: Short
}

object Robots : Table<DbRobot>("robots") {
    val id = long("id").primaryKey().bindTo { it.id }
    val game = long("game_id").references(Games) { it.game }
    val model = enum<RobotModel>("model").bindTo { it.model }
    val facing = enum<Direction>("facing").bindTo { it.facing }
    val damage = short("damage").bindTo { it.damage }
    val poweredDown = boolean("poweredDown").bindTo { it.poweredDown }
    val passedCheckpoints = short("passedCheckpoints").bindTo { it.passedCheckpoints }
}

val Database.robots get() = this.sequenceOf(Robots)

data class Robot(val model: RobotModel, val id: String = RandomStringUtils.randomAlphanumeric(5)) {
    var facing = Direction.DOWN

    private var registers = mutableMapOf<Int, MovementCard?>(
        1 to null,
        2 to null,
        3 to null,
        4 to null,
        5 to null,
    )

    var damage = 0

    var poweredDown = false

    val modifications = emptyList<ModificationCard>()

    var passedCheckpoints = 0

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
     * Check if a register is locked
     */
    fun isLocked(register: Int): Boolean =
        damage >= 5 && register == 5
                || damage >= 6 && register == 4
                || damage >= 7 && register == 3
                || damage >= 8 && register == 2
                || damage >= 9

    /**
     * Clear all registers.
     * @param respectDamageLock If true, will not clear registers that should be locked based
     * on the current damage value of this robot
     */
    fun clearRegisters(respectDamageLock: Boolean = true) {
        val newRegisters = mutableMapOf<Int, MovementCard?>()
        newRegisters.putAll(registers)

        if (respectDamageLock) {
            if (damage <= 8) {
                newRegisters[1] = null
            }

            if (damage <= 7) {
                newRegisters[2] = null
            }

            if (damage <= 6) {
                newRegisters[3] = null
            }

            if (damage <= 5) {
                newRegisters[4] = null
            }

            if (damage <= 4) {
                newRegisters[5] = null
            }
        } else {
            newRegisters.clear()
        }

        registers = newRegisters
    }

    /**
     * Returns true if the robot has movement cards in all registers
     */
    fun hasAllRegistersFilled(): Boolean = registers.all { it.value != null }

    /**
     * Return the movement card in a register (or null if no card is set)
     */
    fun getRegister(register: Int) = registers[register]

    /**
     * Apply a rotational movement on this robot, if the given movement requires it.
     * Non-rotating movements are ignored
     */
    fun rotate(movement: Movement) {
        if (movement == Movement.TURN_RIGHT) {
            facing = when (facing) {
                Direction.UP -> Direction.RIGHT
                Direction.DOWN -> Direction.LEFT
                Direction.LEFT -> Direction.UP
                Direction.RIGHT -> Direction.DOWN
                else -> Direction.NONE
            }
        }

        if (movement == Movement.TURN_LEFT) {
            facing = when (facing) {
                Direction.UP -> Direction.LEFT
                Direction.DOWN -> Direction.RIGHT
                Direction.LEFT -> Direction.DOWN
                Direction.RIGHT -> Direction.UP
                else -> Direction.NONE
            }
        }

        if (movement == Movement.TURN_180) {
            facing = when (facing) {
                Direction.UP -> Direction.DOWN
                Direction.DOWN -> Direction.UP
                Direction.LEFT -> Direction.RIGHT
                Direction.RIGHT -> Direction.LEFT
                else -> Direction.NONE
            }
        }

        // Any other movement don't rotate the robot and can be ignored
    }

    /**
     * Get the direction in which to move this robot, based on where it's facing and a movement to apply
     */
    fun getMovementDirection(movement: Movement): Direction {
        // If the movement should be backwards, simply invert the current orientation the robot is facing
        if (setOf(Movement.BACKWARDS).contains(movement)) {
            return when (facing) {
                Direction.UP -> Direction.DOWN
                Direction.DOWN -> Direction.UP
                Direction.LEFT -> Direction.RIGHT
                Direction.RIGHT -> Direction.LEFT
                else -> Direction.NONE
            }
        }

        // Any other movement do result in an actual movement. For those cases, return the direction the robot
        // is facing so it will move (or stay) in that direction
        return facing
    }

    override fun toString() = "Robot($model)"
}
