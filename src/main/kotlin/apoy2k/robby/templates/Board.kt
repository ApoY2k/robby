package apoy2k.robby.templates

import apoy2k.robby.model.*
import kotlinx.html.Entities
import kotlinx.html.HtmlBlockTag
import kotlinx.html.div

/**
 * Combine the field elements that require directions of a field with the directions of said field.
 */
fun Field.directionElementsClass(): String {
    val directions = listOf(this.outgoingDirection)
        .plus(this.incomingDirections)
        .filter { it != Direction.NONE }
        .joinToString("") { it.name.take(1) }
    val elements = this.elements
        .filter { directionElements.contains(it) }
        .map { it.name }
    return elements
        .joinToString(" ") { "${it}_$directions".lowercase() }
}

fun HtmlBlockTag.renderField(field: Field, robot: Robot? = null) {
    div("field ${field.directionElementsClass()}") {
        field.elements
            .filter { overlayElements.contains(it) }
            .forEach { element ->
                div("overlay-${element.name.lowercase()}") {
                    +Entities.nbsp
                }
            }

        if (robot == null) {
            +Entities.nbsp
            return@div
        }

        renderRobot(robot)
    }
}

fun HtmlBlockTag.renderRobot(robot: Robot) {
    div("robot") {
        var modelClasses = ""
        if (robot.poweredDown) {
            modelClasses += " down"
        }

        div("model icon-${robot.model.name.lowercase()} facing-${robot.facing.name.lowercase()}$modelClasses")
        div("arrow arrow-${robot.facing.name.lowercase()}")
        div("flag-${robot.passedCheckpoints}")
        div("damage-${robot.damage}")
    }
}

fun HtmlBlockTag.renderBoard(board: List<List<Field>>, robots: List<Robot>) {
    div("row") {
        div("col") {
            div("board") {
                val rowTemplate = "60px ".repeat(board.count())
                val colTemplate = "60px ".repeat(board[0].count())

                attributes["style"] = "grid-template-rows: $rowTemplate; grid-template-columns: $colTemplate;"

                board.flatten().forEach { field ->
                    renderField(field, robots.firstOrNull { it.id == field.robotId })
                }
            }
        }
    }
}
