package apoy2k.robby.model.predef.board

import apoy2k.robby.model.Direction
import apoy2k.robby.model.Field
import apoy2k.robby.model.FieldElement

fun generateSandboxBoard() = listOf(
    listOf(
        Field.new(listOf(FieldElement.START, FieldElement.LASER), Direction.RIGHT),
        Field.new(),
        Field.new(),
    ),
    listOf(
        Field.new(listOf(FieldElement.START, FieldElement.BELT), Direction.LEFT),
        Field.new(FieldElement.ROTATE),
        Field.new(FieldElement.BELT, Direction.DOWN),
    ),
    listOf(
        Field.new(listOf(FieldElement.START, FieldElement.FLAG)),
        Field.new(FieldElement.FLAG),
        Field.new(FieldElement.FLAG),
    ),
)
