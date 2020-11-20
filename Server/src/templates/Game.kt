package apoy2k.robby.templates

import apoy2k.robby.ATTR_ACTION
import apoy2k.robby.ATTR_BIND
import apoy2k.robby.VIEW_BOARD
import apoy2k.robby.engine.Game
import apoy2k.robby.engine.SwitchFieldCommand
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
        val command = SwitchFieldCommand(field.id)
        attributes[ATTR_ACTION] = command.toString()
    }
}

fun HtmlBlockTag.gameRender(game: Game) {
    div(classes = "row") {
        attributes[ATTR_BIND] = VIEW_BOARD

        div(classes = "col") {
            game.board.cells.forEach {
                div(classes = "row") {
                    it.forEach { fieldRender(it) }
                }
            }
        }
    }
}
