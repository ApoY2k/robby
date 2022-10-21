package apoy2k.robby.model

import org.apache.commons.lang3.RandomStringUtils
import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.enum
import org.ktorm.schema.long

interface DbField : Entity<DbField> {
    companion object : Entity.Factory<DbField>() {
        fun new(type: FieldType, vararg directions: Direction) = DbField {
            this.type = type
            this.outgoingDirection = directions.first()
            this.incomingDirections = directions.drop(1)
        }
    }

    val id: Long
    var type: FieldType
    var positionX: Long
    var positionY: Long
    var conditions: List<FieldCondition>
    var incomingDirections: List<Direction>
    var outgoingDirection: Direction
    var game: DbGame
}

object Fields : Table<DbField>("fields") {
    val id = long("id").primaryKey().bindTo { it.id }
    val gameId = long("game_id").references(Games) { it.game }
    val type = enum<FieldType>("type").bindTo { it.type }
    val conditions = enumList<FieldCondition>("conditions").bindTo { it.conditions }
    val x = long("positionX").bindTo { it.positionX }
    val y = long("positionY").bindTo { it.positionY }
    val incomingDirections = enumList<Direction>("incomingDirections").bindTo { it.incomingDirections }
    val outgoingDirection = enum<Direction>("outgoingDirection").bindTo { it.outgoingDirection }
}

data class Field(val id: String = RandomStringUtils.randomAlphanumeric(5)) {
    var robot: Robot? = null
    var type = FieldType.NONE

    // Conditions that apply to the field, "on top" of its inherent type
    val conditions = mutableListOf<FieldCondition>()

    // For belt fields: The direction this belt is moving towards
    // For laser/pusher fields: The position of the wall that the appliance is attached to
    // For wall fields: Position of a wall
    var outgoingDirection = Direction.NONE

    // For belt fields: Directions where other belts move onto this belt from
    // For wall fields: Position of other walls
    var incomingDirections = listOf<Direction>()

    constructor(type: FieldType) : this() {
        this.type = type
    }

    // Initialize the field with a set of directions.
    // The first direction is considered the "outgoing" direction (for belt, laser and pusher fields)
    // the other directions outgoing (for belt and wall fields)
    constructor(type: FieldType, vararg directions: Direction) : this(type) {
        if (directions.isNotEmpty()) {
            outgoingDirection = directions.first()
            incomingDirections = directions.drop(1)
        }
    }

    // Check if this field has a specific direction
    fun hasDirection(direction: Direction) = getDirections().contains(direction)

    // Get list of all directions, not matter in or outgoing
    fun getDirections() = listOf(outgoingDirection).plus(incomingDirections)
    fun hasVerticalDirection() = getDirections().any { it.isVertical() }
    fun hasHorizontalDirection() = getDirections().any { it.isHorizontal() }

    // Returns the FieldCondition to apply to any fields that might be in line of this lasser
    // on the board when applying laser conditions
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

    override fun toString(): String {
        val directions = getDirections()
        return "Field($id, $directions, $robot)"
    }
}

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
