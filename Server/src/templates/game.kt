package apoy2k.robby.templates

import apoy2k.robby.engine.Command
import apoy2k.robby.engine.CommandLabel
import apoy2k.robby.engine.Game
import apoy2k.robby.model.Field
import apoy2k.robby.model.FieldType
import kotlinx.html.DIV
import kotlinx.html.HtmlBlockTag
import kotlinx.html.div

fun DIV.fieldRender(field: Field) {
    val fieldClass = when (field.type) {
        FieldType.BLANK -> "btn-secondary"
        FieldType.NOT_BLANK -> "btn-primary"
    }

    div(classes = "col btn $fieldClass field") {
        val command = Command(CommandLabel.SWITCHFIELD, field.id)
        attributes["data-socket-action"] = command.toString()
    }
}

fun HtmlBlockTag.gameRender(game: Game) {
    div(classes = "row") {
        attributes["data-socket-bind"] = "game"

        div(classes = "col") {
            game.board.cells.forEach {
                div(classes = "row") {
                    it.forEach { fieldRender(it) }
                }
            }
        }
    }
}
