package apoy2k.robby.model.predef.board

import apoy2k.robby.model.Direction
import apoy2k.robby.model.Field
import apoy2k.robby.model.FieldType

fun generateSandboxBoard() = listOf(
    listOf(
        Field.new(FieldType.LASER, Direction.RIGHT),
        Field.new(),
        Field.new(),
    ),
    listOf(
        Field.new(FieldType.BELT, Direction.LEFT),
        Field.new(FieldType.ROTATE),
        Field.new(FieldType.BELT, Direction.DOWN),
    ),
    listOf(
        Field.new(FieldType.FLAG),
        Field.new(FieldType.FLAG),
        Field.new(FieldType.FLAG),
    ),
)
