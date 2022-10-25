package apoy2k.robby.templates

import apoy2k.robby.model.Direction
import apoy2k.robby.model.Field
import apoy2k.robby.model.Robot
import kotlinx.html.Entities
import kotlinx.html.HtmlBlockTag
import kotlinx.html.div

fun Field.toCssClass(): String {
    val directions = listOf(this.outgoingDirection)
        .plus(this.incomingDirections)
        .filter { it != Direction.NONE }
        .joinToString("") { it.name.take(1) }

    return listOf(this.type.name, directions)
        .filter { it.isNotBlank() }
        .joinToString("_") { it.lowercase() }
}

fun HtmlBlockTag.renderField(field: Field) {
    div("field type-${field.toCssClass()}") {
        field.conditions.forEach { condition ->
            div("condition-${condition.name.lowercase()}") {
                +Entities.nbsp
            }
        }

        val robot = field.robot
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

fun HtmlBlockTag.renderBoard(fields: List<List<Field>>) {
    div("row") {
        div("col") {
            div("board") {
                val rowTemplate = "60px ".repeat(fields.count())
                val colTemplate = "60px ".repeat(fields[0].count())

                attributes["style"] = "grid-template-rows: $rowTemplate; grid-template-columns: $colTemplate;"

                fields.flatten().forEach {
                    renderField(it)
                }
            }
        }
    }
}
