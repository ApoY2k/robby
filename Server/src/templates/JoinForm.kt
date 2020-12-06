package apoy2k.robby.templates

import apoy2k.robby.ATTR_BIND
import apoy2k.robby.model.ActionField
import apoy2k.robby.model.Game
import apoy2k.robby.model.Session
import apoy2k.robby.model.View
import kotlinx.html.*

fun HtmlBlockTag.renderJoinForm(game: Game, session: Session?) {
    div(classes = "row") {
        attributes[ATTR_BIND] = View.JOIN_FORM.toString();

        div(classes = "col") {
            if (game.hasJoined(session)) {
                form(classes = "form", action = "/leave", method = FormMethod.post) {
                    button(classes = "btn btn-primary", type = ButtonType.submit) {
                        +"Leave"
                    }
                }
            } else {
                form(classes = "form", action = "/join", method = FormMethod.post) {
                    div(classes = "form-group") {
                        input(classes = "form-control", name = ActionField.PLAYER_NAME.name)
                    }

                    button(classes = "btn btn-primary", type = ButtonType.submit) {
                        +"Join"
                    }
                }
            }
        }
    }
}
