package apoy2k.robby.model.predef.board

import apoy2k.robby.model.Direction
import apoy2k.robby.model.Field
import apoy2k.robby.model.FieldElement

fun generateLaserTestBoard() = listOf(
    listOf(
        Field.new(),
        Field.new(),
        Field.new(FieldElement.WALL, Direction.LEFT, Direction.DOWN),
        Field.new(FieldElement.WALL, Direction.UP),
    ),
    listOf(
        Field.new(FieldElement.LASER, Direction.UP),
        Field.new(FieldElement.LASER_2, Direction.UP),
        Field.new(),
        Field.new(),
    ),
    listOf(
        Field.new(FieldElement.WALL, Direction.UP),
        Field.new(FieldElement.WALL, Direction.DOWN),
        Field.new(),
        Field.new(),
    ),
    listOf(
        Field.new(),
        Field.new(FieldElement.WALL, Direction.LEFT),
        Field.new(FieldElement.LASER, Direction.DOWN),
        Field.new(FieldElement.LASER_2, Direction.DOWN),
    ),
    listOf(
        Field.new(FieldElement.LASER, Direction.LEFT),
        Field.new(),
        Field.new(FieldElement.WALL, Direction.LEFT),
        Field.new(),
    ),
    listOf(
        Field.new(),
        Field.new(),
        Field.new(),
        Field.new(FieldElement.LASER_2, Direction.RIGHT),
    ),
)
