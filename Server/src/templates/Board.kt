package apoy2k.robby.templates

import apoy2k.robby.ATTR_BIND
import apoy2k.robby.model.*
import kotlinx.html.*

fun HtmlBlockTag.renderOrientation(orientation: Orientation) {
    +when (orientation) {
        Orientation.UP -> "(^)"
        Orientation.RIGHT -> "(>)"
        Orientation.DOWN -> "(v)"
        Orientation.LEFT -> "(<)"
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
            renderOrientation(robot.orientation)
        }
    }
}

fun HtmlBlockTag.renderBoard(game: Game, session: Session?) {
    div(classes = "row") {
        attributes[ATTR_BIND] = View.BOARD.toString()

        div(classes = "col") {
            game.board.fields.forEach {
                div(classes = "row") {
                    it.forEach { renderField(game, it, session) }
                }
            }
        }
    }
}
