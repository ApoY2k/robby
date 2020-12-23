package apoy2k.robby.model.predef.board

import apoy2k.robby.model.Direction
import apoy2k.robby.model.Field
import apoy2k.robby.model.FieldType

val DEMO_BOARD = listOf(
    listOf(
        Field(),
        Field(),
        Field(),
        Field(),
        Field(),
        Field(),
        Field(),
        Field(),
        Field(),
        Field(),
        Field(),
        Field(),
    ),
    listOf(
        Field(FieldType.BELT, Direction.DOWN),
        Field(FieldType.BELT, Direction.DOWN, Direction.LEFT),
        Field(FieldType.BELT, Direction.DOWN, Direction.RIGHT),
        Field(FieldType.BELT, Direction.UP),
        Field(FieldType.BELT, Direction.UP, Direction.LEFT),
        Field(FieldType.BELT, Direction.UP, Direction.RIGHT),
        Field(FieldType.BELT, Direction.LEFT),
        Field(FieldType.BELT, Direction.LEFT, Direction.UP),
        Field(FieldType.BELT, Direction.LEFT, Direction.DOWN),
        Field(FieldType.BELT, Direction.RIGHT),
        Field(FieldType.BELT, Direction.RIGHT, Direction.UP),
        Field(FieldType.BELT, Direction.RIGHT, Direction.DOWN),
    ),
    listOf(
        Field(FieldType.BELT, Direction.DOWN, Direction.LEFT, Direction.RIGHT),
        Field(FieldType.BELT, Direction.UP, Direction.LEFT, Direction.RIGHT),
        Field(FieldType.BELT, Direction.LEFT, Direction.DOWN, Direction.UP),
        Field(FieldType.BELT, Direction.RIGHT, Direction.DOWN, Direction.UP),
        Field(FieldType.BELT_2, Direction.DOWN),
        Field(FieldType.BELT_2, Direction.DOWN, Direction.LEFT),
        Field(FieldType.BELT_2, Direction.DOWN, Direction.RIGHT),
        Field(FieldType.BELT_2, Direction.UP),
        Field(FieldType.BELT_2, Direction.UP, Direction.LEFT),
        Field(FieldType.BELT_2, Direction.UP, Direction.RIGHT),
        Field(FieldType.BELT_2, Direction.LEFT),
        Field(FieldType.BELT_2, Direction.LEFT, Direction.UP),
    ),
    listOf(
        Field(FieldType.BELT_2, Direction.LEFT, Direction.DOWN),
        Field(FieldType.BELT_2, Direction.RIGHT),
        Field(FieldType.BELT_2, Direction.RIGHT, Direction.UP),
        Field(FieldType.BELT_2, Direction.RIGHT, Direction.DOWN),
        Field(FieldType.BELT_2, Direction.DOWN, Direction.LEFT, Direction.RIGHT),
        Field(FieldType.BELT_2, Direction.UP, Direction.LEFT, Direction.RIGHT),
        Field(FieldType.BELT_2, Direction.LEFT, Direction.DOWN, Direction.UP),
        Field(FieldType.BELT_2, Direction.RIGHT, Direction.DOWN, Direction.UP),
        Field(FieldType.ROTATE_LEFT),
        Field(FieldType.ROTATE_RIGHT),
        Field(FieldType.FLAG),
        Field(FieldType.HOLE),
    ),
    listOf(
        Field(FieldType.WALL, Direction.DOWN),
        Field(FieldType.WALL, Direction.DOWN, Direction.LEFT),
        Field(FieldType.WALL, Direction.DOWN, Direction.LEFT, Direction.RIGHT),
        Field(FieldType.WALL, Direction.DOWN, Direction.LEFT, Direction.RIGHT, Direction.UP),
        Field(FieldType.WALL, Direction.DOWN, Direction.LEFT, Direction.UP),
        Field(FieldType.WALL, Direction.DOWN, Direction.RIGHT),
        Field(FieldType.WALL, Direction.DOWN, Direction.RIGHT, Direction.UP),
        Field(FieldType.WALL, Direction.DOWN, Direction.UP),
        Field(FieldType.WALL, Direction.UP),
        Field(FieldType.WALL, Direction.UP, Direction.LEFT),
        Field(FieldType.WALL, Direction.UP, Direction.LEFT, Direction.RIGHT),
        Field(FieldType.WALL, Direction.UP, Direction.RIGHT),
    ),
    listOf(
        Field(FieldType.WALL, Direction.LEFT),
        Field(FieldType.WALL, Direction.LEFT, Direction.RIGHT),
        Field(FieldType.WALL, Direction.RIGHT),
        Field(FieldType.LASER, Direction.DOWN),
        Field(FieldType.LASER, Direction.UP),
        Field(FieldType.LASER, Direction.LEFT),
        Field(FieldType.LASER, Direction.RIGHT),
        Field(FieldType.LASER_2, Direction.DOWN),
        Field(FieldType.LASER_2, Direction.UP),
        Field(FieldType.LASER_2, Direction.LEFT),
        Field(FieldType.LASER_2, Direction.RIGHT),
        Field(FieldType.PUSHER, Direction.DOWN),
    ),
    listOf(
        Field(FieldType.PUSHER, Direction.UP),
        Field(FieldType.PUSHER, Direction.LEFT),
        Field(FieldType.PUSHER, Direction.RIGHT),
        Field(FieldType.REPAIR),
        Field(FieldType.REPAIR_MOD),
        Field(),
        Field(),
        Field(),
        Field(),
        Field(),
        Field(),
        Field(),
    )
)
