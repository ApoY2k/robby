package apoy2k.robby.templates

import apoy2k.robby.model.*
import kotlinx.html.*

fun HtmlBlockTag.renderOrientation(direction: Direction) {
    +when (direction) {
        Direction.UP -> "(^)"
        Direction.RIGHT -> "(>)"
        Direction.DOWN -> "(v)"
        Direction.LEFT -> "(<)"
    }
}

fun HtmlBlockTag.renderField(game: Game, field: Field, session: Session?) {
    div("field") {
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
    div(classes = "robot robot-${robot.model.name.toLowerCase()} direction-${robot.facing.name.toLowerCase()}")
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
