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
    div(classes = "col m-1 field") {
        val playerRobot = game.playerFor(session)?.robot
        val robot = field.robot
        if (robot == null) {
            entity(Entities.nbsp)
            return@div
        }

        var classes = ""
        if (robot == playerRobot) {
            classes = "text-success"
        }

        span(classes = classes) {
            +robot.model.name
            entity(Entities.nbsp)
            renderOrientation(robot.facing)
        }
    }
}

fun HtmlBlockTag.renderBoard(game: Game, session: Session?) {
    div(classes = "row") {
        div(classes = "col") {
            game.board.fields.forEach {
                div(classes = "row") {
                    it.forEach { renderField(game, it, session) }
                }
            }
        }
    }
}
