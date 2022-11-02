package apoy2k.robby.model

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.enum
import org.ktorm.schema.int

enum class FieldElement {
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
    START,
    LASER_H,
    LASER_V,
    LASER_2_H,
    LASER_2_V;

    fun isBlocking() = this == WALL || this == LASER || this == LASER_2 || this == PUSHER
}

object Fields : Table<Field>("fields") {
    val id = int("id").primaryKey().bindTo { it.id }
    val gameId = int("game_id").bindTo { it.gameId }
    val robotId = int("robot_id").bindTo { it.robotId }
    val elements = enumList<FieldElement>("elements").bindTo { it.elements }
    val positionX = int("positionX").bindTo { it.positionX }
    val positionY = int("positionY").bindTo { it.positionY }
    val incomingDirections = enumList<Direction>("incomingDirections").bindTo { it.incomingDirections }
    val outgoingDirection = enum<Direction>("outgoingDirection").bindTo { it.outgoingDirection }
}

interface Field : Entity<Field> {
    companion object : Entity.Factory<Field>() {

        @JvmStatic
        fun new(element: FieldElement? = null, vararg directions: Direction) = Field {
            this.elements = when (element) {
                null -> mutableListOf()
                else -> mutableListOf(element)
            }
            this.outgoingDirection = directions.firstOrNull() ?: Direction.NONE
            this.incomingDirections = directions.drop(1)
        }

        @JvmStatic
        fun new(elements: List<FieldElement>, vararg directions: Direction) = Field {
            this.elements = elements.toMutableList()
            this.outgoingDirection = directions.firstOrNull() ?: Direction.NONE
            this.incomingDirections = directions.drop(1)
        }
    }

    var id: Int
    var gameId: Int
    var robotId: Int?
    var elements: MutableList<FieldElement>
    var positionX: Int
    var positionY: Int

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
    fun getInLineLaserFieldElements(): FieldElement? {
        if (elements.contains(FieldElement.LASER)) {
            return when (outgoingDirection) {
                Direction.DOWN, Direction.UP -> FieldElement.LASER_V
                Direction.LEFT, Direction.RIGHT -> FieldElement.LASER_H
                else -> null
            }
        }

        if (elements.contains(FieldElement.LASER_2)) {
            return when (outgoingDirection) {
                Direction.DOWN, Direction.UP -> FieldElement.LASER_2_V
                Direction.LEFT, Direction.RIGHT -> FieldElement.LASER_2_H
                else -> null
            }
        }

        return null
    }

    /**
     * true, if this field contains any blocking elements
     */
    fun hasBlockingELement() = elements.any { it.isBlocking() }
}
