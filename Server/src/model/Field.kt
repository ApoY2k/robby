package apoy2k.robby.model

import java.util.*

enum class FieldType {
    NONE,
    HOLE,
    WALL,
    BELT,
    BELT_2,
    LASER,
    LASER_2,
    ROTATE_LEFT,
    ROTATE_RIGHT,
    PUSHER,
    FLAG,
    REPAIR,
    REPAIR_MOD,
}

data class Field(val id: UUID = UUID.randomUUID()) {
    var robot: Robot? = null
    var type = FieldType.NONE
    var directions = EnumSet.noneOf(Direction::class.java)

    constructor(type: FieldType) : this() {
        this.type = type
    }

    constructor(type: FieldType, direction: Direction): this(type) {
        this.directions = EnumSet.of(direction)
    }

    constructor(type: FieldType, vararg directions: Direction): this(type) {
        this.directions = EnumSet.copyOf(directions.asList())
    }
}
