package apoy2k.robby.templates

import apoy2k.robby.model.*
import kotlinx.html.*

fun HtmlBlockTag.renderJoinForm(game: Game, session: Session?) {
    div("row") {
        div("col") {
            form("form") {
                if (game.hasJoined(session)) {
                    attributes["data-action"] = LeaveGameAction().serializeForSocket()

                    button(classes = "btn btn-primary", type = ButtonType.submit) {
                        +"Leave"
                    }
                } else {
                    attributes["data-action"] = JoinGameAction().serializeForSocket()

                    div("form-group") {
                        select("form-control") {
                            attributes["name"] = ActionField.ROBOT_MODEL.name

                            RobotModel.values()
                                .filter { !game.players.mapNotNull { it.robot?.model }.contains(it) }
                                .forEach {
                                    option {
                                        +it.name
                                    }
                                }
                        }
                    }

                    button(classes = "btn btn-primary", type = ButtonType.submit) {
                        +"Join"
                    }
                }
            }
        }
    }
}
