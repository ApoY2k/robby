package apoy2k.robby.templates

import apoy2k.robby.model.*
import kotlinx.html.*

fun HtmlBlockTag.renderJoinForm(game: Game, session: Session?) {
    div(classes = "row") {
        div(classes = "col") {
            form(classes = "form") {
                if (game.hasJoined(session)) {
                    attributes["data-action"] = LeaveGameAction().toString()

                    button(classes = "btn btn-primary", type = ButtonType.submit) {
                        +"Leave"
                    }
                } else {
                    attributes["data-action"] = JoinGameAction().toString()

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
