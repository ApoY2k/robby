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
    FLAG_1,
    FLAG_2,
    FLAG_3,
    FLAG_4,
    FLAG_5,
    FLAG_6,
    FLAG_7,
    FLAG_8,
    REPAIR,
    REPAIR_MOD,
    START_1,
    START_2,
    START_3,
    START_4,
    START_5,
    START_6,
    START_7,
    START_8,
    LASER_H,
    LASER_V,
    LASER_2_H,
    LASER_2_V,
    ROBOT_LASER_V,
    ROBOT_LASER_H,
}

// List of field elements that need to be combined with the directions of a field
val directionElements = listOf(
    FieldElement.BELT,
    FieldElement.BELT_2,
    FieldElement.WALL,
    FieldElement.LASER,
    FieldElement.LASER_2,
    FieldElement.PUSHER,
    FieldElement.ROTATE,
)

// List of field elements that are overlayed on top of the direction elements
val overlayElements = listOf(
    FieldElement.FLAG_1,
    FieldElement.FLAG_2,
    FieldElement.FLAG_3,
    FieldElement.FLAG_4,
    FieldElement.FLAG_5,
    FieldElement.FLAG_6,
    FieldElement.FLAG_7,
    FieldElement.FLAG_8,
    FieldElement.START_1,
    FieldElement.START_2,
    FieldElement.START_3,
    FieldElement.START_4,
    FieldElement.START_5,
    FieldElement.START_6,
    FieldElement.START_7,
    FieldElement.START_8,
    FieldElement.REPAIR,
    FieldElement.REPAIR_MOD,
    FieldElement.LASER_H,
    FieldElement.LASER_V,
    FieldElement.LASER_2_H,
    FieldElement.LASER_2_V,
    FieldElement.HOLE,
    FieldElement.ROBOT_LASER_H,
    FieldElement.ROBOT_LASER_V,
)

// List of laser overlay field elements
val laserOverlays = listOf(
    FieldElement.LASER_H,
    FieldElement.LASER_V,
    FieldElement.LASER_2_H,
    FieldElement.LASER_2_V,
    FieldElement.ROBOT_LASER_H,
    FieldElement.ROBOT_LASER_V,
)

// List of appliance elements that are attached to walls
val applianceElements = listOf(
    FieldElement.LASER,
    FieldElement.LASER_2,
    FieldElement.PUSHER,
)

// List of all flag elements
val flagElements = listOf(
    FieldElement.FLAG_1,
    FieldElement.FLAG_2,
    FieldElement.FLAG_3,
    FieldElement.FLAG_4,
    FieldElement.FLAG_5,
    FieldElement.FLAG_6,
    FieldElement.FLAG_7,
    FieldElement.FLAG_8,
)

// List of all start elements
val startElements = listOf(
    FieldElement.START_1,
    FieldElement.START_2,
    FieldElement.START_3,
    FieldElement.START_4,
    FieldElement.START_5,
    FieldElement.START_6,
    FieldElement.START_7,
    FieldElement.START_8,
)

@Suppress("unused")
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
            this.outgoingDirection = directions.firstOrNull()
            this.incomingDirections = directions.drop(1)
        }

        @JvmStatic
        fun new(elements: List<FieldElement>, vararg directions: Direction) = Field {
            this.elements = elements.toMutableList()
            this.outgoingDirection = directions.firstOrNull()
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
    var outgoingDirection: Direction?

    /**
     * Get list of all directions, no matter in or outgoing
     */
    fun getDirections() = listOf(outgoingDirection).plus(incomingDirections)

    /**
     * Check if this field has a specific direction
     */
    fun hasDirection(direction: Direction) = getDirections().contains(direction)

    /**
     * True, if this field has *any one* of the provided directions
     */
    fun hasAnyDirection(vararg directions: Direction) = getDirections().any { directions.contains(it) }

    /**
     * True if the field contains a vertical wall element
     */
    fun hasVerticalWall() = hasAnyDirection(Direction.LEFT, Direction.RIGHT)
            && (elements.contains(FieldElement.WALL) || applianceElements.any { elements.contains(it) })

    /**
     * True if the field contains a horizontal wall element
     */
    fun hasHorizontalWall() = hasAnyDirection(Direction.UP, Direction.DOWN)
            && (elements.contains(FieldElement.WALL) || applianceElements.any { elements.contains(it) })

    /**
     * True, if this field blocks a vertical laser from *entering* the field
     */
    fun blocksVerticalLaserEntry(entryDirection: Direction) = hasHorizontalWall()
            && (robotId != null || hasDirection(entryDirection))

    /**
     * True, if this field blocks a vertical laser from *exiting* the field
     */
    fun blocksVerticalLaserExit(entryDirection: Direction) = hasHorizontalWall()
            && (robotId != null || hasDirection(entryDirection.toOpposite()))

    /**
     * True, if this field blocks a vertical laser from *entering* the field
     */
    fun blocksHorizontalLaserEntry(entryDirection: Direction) = hasVerticalWall()
            && (robotId != null || hasDirection(entryDirection))

    /**
     * True, if this field blocks a vertical laser from *exiting* the field
     */
    fun blocksHorizontalLaserExit(entryDirection: Direction) = hasVerticalWall()
            && (robotId != null || hasDirection(entryDirection.toOpposite()))

    /**
     * True, if this field has a wall on the given side
     */
    fun hasWallOn(direction: Direction) = elements.contains(FieldElement.WALL) && hasDirection(direction)

    /**
     * Returns the FieldElement to apply to any fields that might be in line of this lasser type
     * on the board when applying laser elements
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
     * True, if this field has any flag element on it
     */
    fun hasFlag() = flagElements.any { elements.contains(it) }

    /**
     * Returns the number of the first flag element on this field
     */
    fun getFlagNumber() = flagElements.first { elements.contains(it) }
        .name
        .takeLast(1)
        .toInt()

    /**
     * True, if this field has any start element on it
     */
    fun hasStart() = startElements.any { elements.contains(it) }

    /**
     * Returns the number of the first start element on this field
     */
    fun getStartNumber() = startElements.first { elements.contains(it) }
        .name
        .takeLast(1)
        .toInt()

    /**
     * True, if the field contains any laser overlay element
     */
    fun hasLaserOverlay() = elements.any { laserOverlays.contains(it) }

    /**
     * Clear any laser overlays on this field
     */
    fun clearLaserOverlays() = elements.removeIf { el -> laserOverlays.contains(el) }

    /**
     * True if this field has any laser-emitter on it
     */
    fun hasLaserEmitter() = elements.contains(FieldElement.LASER)
            || elements.contains(FieldElement.LASER_2)
            || robotId != null
}
