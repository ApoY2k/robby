package apoy2k.robby.model.predef.board

import apoy2k.robby.model.Direction
import apoy2k.robby.model.Field
import apoy2k.robby.model.FieldElement

fun generateSandboxBoard() = listOf(
    listOf(
        Field.new(listOf(FieldElement.START_1, FieldElement.LASER), Direction.RIGHT),
        Field.new(),
        Field.new(),
    ),
    listOf(
        Field.new(listOf(FieldElement.START_2, FieldElement.BELT), Direction.LEFT),
        Field.new(FieldElement.ROTATE),
        Field.new(FieldElement.BELT, Direction.DOWN),
    ),
    listOf(
        Field.new(listOf(FieldElement.START_3, FieldElement.FLAG_1)),
        Field.new(FieldElement.FLAG_2),
        Field.new(FieldElement.FLAG_3),
    ),
)
