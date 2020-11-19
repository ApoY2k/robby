package apoy2k.robby.templates

import apoy2k.robby.engine.Game
import apoy2k.robby.model.Field
import apoy2k.robby.model.FieldType
import kotlinx.html.*

fun DIV.fieldRender(field: Field) {
    val fieldClass = when (field.type) {
        FieldType.BLANK -> "btn btn-secondary"
        FieldType.NOT_BLANK -> "btn btn-primary"
    }

    div(classes = "col $fieldClass field") {
        attributes["data-id"] = field.id
    }
}

fun HtmlBlockTag.gameRender(game: Game) {
    div(classes = "row") {
        div(classes = "col") {
            game.board.cells.forEach {
                div(classes = "row") {
                    it.forEach { fieldRender(it) }
                }
            }
        }
    }
}
