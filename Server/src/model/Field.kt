package apoy2k.robby.model

import java.util.*

data class Field(val id: UUID = UUID.randomUUID()) {
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

    // true, if this field has any directions, no matter in or outgoing
    fun hasDirections() = getDirections().isNotEmpty()

    override fun toString(): String {
        val directions = getDirections()

        val str = when (type) {
            FieldType.NONE -> " "
            FieldType.HOLE -> "O"
            FieldType.WALL -> {
                var result = " "

                if (directions.contains(Direction.LEFT)) {
                    result += "|"
                }

                if (directions.contains(Direction.UP) && directions.contains(Direction.DOWN)) {
                    result += "="
                } else {
                    if (directions.contains(Direction.UP)) {
                        result += "¯"
                    }

                    if (directions.contains(Direction.DOWN)) {
                        result += "_"
                    }
                }

                if (directions.contains(Direction.RIGHT)) {
                    result += "|"
                }

                result
            }
            FieldType.BELT -> {
                val inc = incomingDirections.joinToString {
                    when (it) {
                        Direction.DOWN -> "v"
                        Direction.LEFT -> "<"
                        Direction.RIGHT -> ">"
                        Direction.UP -> "^"
                        else -> " "
                    }
                }

                val out = "" + when (outgoingDirection) {
                    Direction.DOWN -> "v"
                    Direction.LEFT -> "<"
                    Direction.RIGHT -> ">"
                    Direction.UP -> "^"
                    else -> " "
                }

                "$inc($out)"
            }
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

        return "[$str](${robot ?: " "})"
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
