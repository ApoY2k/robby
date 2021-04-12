package apoy2k.robby.model

import java.util.*

data class Field(val id: UUID = UUID.randomUUID()) {
    var robot: Robot? = null
    var type = FieldType.NONE

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

    // Get list of all directions, not matter in or outgoing
    fun getDirections() = listOf(outgoingDirection).plus(incomingDirections)

    // true, if this field has any directions, no matter in or outgoing
    fun hasDirections() = getDirections().isNotEmpty()

    override fun toString(): String {
        val directions = getDirections()

        val str = when (type) {
            FieldType.NONE -> " "
            FieldType.HOLE -> "O"
            FieldType.WALL -> when {
                directions.count() == 4 -> ""
                directions.contains(Direction.UP) -> "╵"
                directions.contains(Direction.LEFT) -> "╴"
                directions.contains(Direction.RIGHT) -> "╶"
                directions.contains(Direction.DOWN) -> "╷"
                else -> " "
            }
            FieldType.BELT -> " "
            FieldType.BELT_2 -> " "
            FieldType.LASER -> " "
            FieldType.LASER_2 -> " "
            FieldType.ROTATE -> when (outgoingDirection) {
                Direction.RIGHT -> "↻"
                Direction.LEFT -> "↺"
                else -> " "
            }
            FieldType.FLAG -> "\uD83D\uDEA9"
            FieldType.REPAIR -> " "
            FieldType.REPAIR_MOD -> " "
            else -> " "
        }

        return "[$str]"
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
    REPAIR_MOD,
}
