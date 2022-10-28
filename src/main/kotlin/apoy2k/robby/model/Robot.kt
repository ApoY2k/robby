package apoy2k.robby.model

import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.Entity
import org.ktorm.entity.filter
import org.ktorm.entity.map
import org.ktorm.schema.*

enum class RobotModel {
    ZIPPY,
    GEROG,
    KLAUS,
    HUZZA,
}

object Robots : Table<Robot>("robots") {
    val id = int("id").primaryKey().bindTo { it.id }
    val gameId = int("game_id").bindTo { it.gameId }
    val session = varchar("session").bindTo { it.sessionId }
    val name = varchar("name").bindTo { it.name }
    val ready = boolean("ready").bindTo { it.ready }
    val model = enum<RobotModel>("model").bindTo { it.model }
    val facing = enum<Direction>("facing").bindTo { it.facing }
    val damage = int("damage").bindTo { it.damage }
    val poweredDown = boolean("poweredDown").bindTo { it.poweredDown }
    val powerDownScheduled = boolean("powerDownScheduled").bindTo { it.powerDownScheduled }
    val passedCheckpoints = int("passedCheckpoints").bindTo { it.passedCheckpoints }
}

interface Robot : Entity<Robot> {
    companion object : Entity.Factory<Robot>() {
        fun new(model: RobotModel) = new(model, Session("", ""))
        fun new(model: RobotModel, session: Session) = Robot {
            this.model = model
            this.name = session.name
            this.sessionId = session.id
        }
    }

    var id: Int
    var gameId: Int
    var sessionId: String
    var name: String
    var ready: Boolean
    var model: RobotModel
    var facing: Direction
    var damage: Int
    var powerDownScheduled: Boolean
    var poweredDown: Boolean
    var passedCheckpoints: Int

    fun cards(db: Database) = db.cards.filter { it.robotId eq id }.map { it }
}

/**
 * Toggle power down for the round
 */
fun Robot.togglePowerDown() {
    powerDownScheduled = !powerDownScheduled
}

/**
 * Toggle ready state
 */
fun Robot.toggleReady() {
    ready = !ready
}

/**
 * Check if a register is locked
 */
fun Robot.isLocked(register: Int): Boolean =
    damage >= 5 && register == 5
            || damage >= 6 && register == 4
            || damage >= 7 && register == 3
            || damage >= 8 && register == 2
            || damage >= 9

/**
 * Apply a rotational movement on this robot, if the given movement requires it.
 * Non-rotating movements are ignored
 */
fun Robot.rotate(movement: Movement) {
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
fun Robot.getMovementDirection(movement: Movement): Direction {
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
    // is facing, so it will move (or stay) in that direction
    return facing
}
