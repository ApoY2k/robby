package apoy2k.robby.model.predef.board

import apoy2k.robby.model.Direction
import apoy2k.robby.model.Field
import apoy2k.robby.model.FieldType

fun generateSandboxBoard() = listOf(
    listOf(
        Field(FieldType.LASER, Direction.RIGHT),
        Field(),
        Field(),
    ),
    listOf(
        Field(FieldType.BELT, Direction.LEFT),
        Field(FieldType.ROTATE),
        Field(FieldType.BELT, Direction.DOWN),
    ),
    listOf(
        Field(FieldType.FLAG),
        Field(FieldType.FLAG),
        Field(FieldType.FLAG),
    ),
)
