package apoy2k.robby.model

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.enum
import org.ktorm.schema.int

enum class FieldType {
    NONE,
    HOLE,
    WALL,
    BELT,
    BELT_2,
    LASER,
    LASER_2,
    ROTATE,
    PUSHER,
    FLAG,
    REPAIR,
    REPAIR_MOD;

    fun isBlocking() = this == WALL || this == LASER || this == LASER_2 || this == PUSHER
}

enum class FieldCondition {
    LASER_H,
    LASER_V,
    LASER_2_H,
    LASER_2_V,
}

object Fields : Table<Field>("fields") {
    val id = int("id").primaryKey().bindTo { it.id }
    val gameId = int("game_id").bindTo { it.gameId }
    val robotId = int("robot_id").bindTo { it.robotId }
    val type = enum<FieldType>("type").bindTo { it.type }
    val conditions = enumList<FieldCondition>("conditions").bindTo { it.conditions }
    val positionX = int("positionX").bindTo { it.positionX }
    val positionY = int("positionY").bindTo { it.positionY }
    val incomingDirections = enumList<Direction>("incomingDirections").bindTo { it.incomingDirections }
    val outgoingDirection = enum<Direction>("outgoingDirection").bindTo { it.outgoingDirection }
}

interface Field : Entity<Field> {
    companion object : Entity.Factory<Field>() {
        fun new(type: FieldType = FieldType.NONE, vararg directions: Direction) = Field {
            this.type = type
            this.conditions = mutableListOf()
            this.outgoingDirection = directions.firstOrNull() ?: Direction.NONE
            this.incomingDirections = directions.drop(1)
        }
    }

    var id: Int
    var gameId: Int
    var robotId: Int?
    var type: FieldType
    var positionX: Int
    var positionY: Int

    /**
     * Conditions that apply to the field, "on top" of its inherent type
     */
    var conditions: MutableList<FieldCondition>

    /**
     * For belt fields: Directions where other belts move onto this belt from
     * For wall fields: Position of other walls
     */
    var incomingDirections: List<Direction>

    /**
     * For belt fields: The direction this belt is moving towards
     * For laser/pusher fields: The position of the wall that the appliance is attached to
     * For wall fields: Position of a wall
     */
    var outgoingDirection: Direction

    /**
     * Get list of all directions, no matter in or outgoing
     */
    fun getDirections() = listOf(outgoingDirection).plus(incomingDirections)

    /**
     * Check if this field has a specific direction
     */
    fun hasDirection(direction: Direction) = getDirections().contains(direction)

    /**
     * True if any direction on the field is vertical
     */
    fun hasVerticalDirection() = getDirections().any { it.isVertical() }

    /**
     * True if any direction on the field is horizontal
     */
    fun hasHorizontalDirection() = getDirections().any { it.isHorizontal() }

    /**
     * Returns the FieldCondition to apply to any fields that might be in line of this lasser type
     * on the board when applying laser conditions
     */
    fun getInLineLaserFieldsCondition() = when (type) {
        FieldType.LASER -> when (outgoingDirection) {
            Direction.DOWN, Direction.UP -> FieldCondition.LASER_V
            Direction.LEFT, Direction.RIGHT -> FieldCondition.LASER_H
            else -> null
        }

        FieldType.LASER_2 -> when (outgoingDirection) {
            Direction.DOWN, Direction.UP -> FieldCondition.LASER_2_V
            Direction.LEFT, Direction.RIGHT -> FieldCondition.LASER_2_H
            else -> null
        }

        else -> null
    }
}
