package apoy2k.robby.templates

import apoy2k.robby.model.*
import kotlinx.html.*

fun Field.directionsToCssClass(): String =
    listOf(this.outgoingDirection)
        .plus(this.incomingDirections)
        .joinToString("") { it.name.take(1).toLowerCase() }

fun HtmlBlockTag.renderField(game: Game, field: Field, session: Session?) {
    div("field type-${field.type.name.toLowerCase()}") {
        if (field.hasDirections()) {
            attributes["class"] += "_${field.directionsToCssClass()}"
        }

        val playerRobot = game.playerFor(session)?.robot
        val robot = field.robot
        if (robot == null) {
            entity(Entities.nbsp)
            return@div
        }

        renderRobot(robot)
    }
}

fun HtmlBlockTag.renderRobot(robot: Robot) {
    div("robot") {
        div("model icon-${robot.model.name.toLowerCase()} facing-${robot.facing.name.toLowerCase()}")
        div("arrow arrow-${robot.facing.name.toLowerCase()}")
    }
}

fun HtmlBlockTag.renderBoard(game: Game, session: Session?) {
    div("row") {
        div("col") {
            div("board") {
                val rowTemplate = "60px ".repeat(game.board.fields.count())
                val colTemplate = "60px ".repeat(game.board.fields[0].count())

                attributes["style"] = "grid-template-rows: $rowTemplate; grid-template-columns: $colTemplate;"

                game.board.fields.flatten().forEach {
                    renderField(game, it, session)
                }
            }
        }
    }
}
