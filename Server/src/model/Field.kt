package apoy2k.robby.model

import java.util.*

data class Field(val id: String = UUID.randomUUID().toString()) {
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
        return "Field(directions=$directions, robot=$robot)"
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
