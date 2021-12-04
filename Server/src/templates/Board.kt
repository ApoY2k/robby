package apoy2k.robby.templates

import apoy2k.robby.model.Field
import apoy2k.robby.model.Game
import apoy2k.robby.model.Robot
import apoy2k.robby.model.Session
import kotlinx.html.Entities
import kotlinx.html.HtmlBlockTag
import kotlinx.html.div

fun Field.directionsToCssClass(): String =
    listOf(this.outgoingDirection)
        .plus(this.incomingDirections)
        .joinToString("") { it.name.take(1).lowercase() }

fun HtmlBlockTag.renderField(game: Game, field: Field, session: Session?) {
    div("field type-${field.type.name.lowercase()}") {
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
        div("model icon-${robot.model.name.lowercase()} facing-${robot.facing.name.lowercase()}")
        div("arrow arrow-${robot.facing.name.lowercase()}")
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
