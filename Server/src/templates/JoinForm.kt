package apoy2k.robby.templates

import apoy2k.robby.ATTR_ACTION
import apoy2k.robby.ATTR_BIND
import apoy2k.robby.model.*
import kotlinx.html.*

fun HtmlBlockTag.renderJoinForm(game: Game, session: Session?) {
    div(classes = "row") {
        attributes[ATTR_BIND] = View.JOIN_FORM.toString();

        div(classes = "col") {
            form(classes = "form") {
                if (game.hasJoined(session)) {
                    attributes[ATTR_ACTION] = LeaveGameAction().toString()

                    button(classes = "btn btn-primary", type = ButtonType.submit) {
                        +"Leave"
                    }
                } else {
                    attributes[ATTR_ACTION] = JoinGameAction().toString()

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
