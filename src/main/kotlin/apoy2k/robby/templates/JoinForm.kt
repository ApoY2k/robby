package apoy2k.robby.templates

import apoy2k.robby.model.*
import kotlinx.html.*

fun HtmlBlockTag.renderJoinForm(game: Game, session: Session?) {
    div("row") {
        div("col") {
            div("card") {
                div("card-body") {
                    form("form row") {
                        if (game.hasJoined(session)) {
                            attributes["data-action"] = LeaveGameAction().serializeForSocket()
                            button(classes = "btn btn-primary", type = ButtonType.submit) { +"Leave game" }
                            return@form
                        }

                        if (game.hasStarted) {
                            p("alert alert-info") { +"You have not joined in the game. Specating only" }
                            return@form
                        }

                        joinForm(game)
                    }
                }
            }
        }
    }
}

fun HtmlBlockTag.joinForm(game: Game) {
    attributes["data-action"] = JoinGameAction().serializeForSocket()

    p { +"The game is open to join." }
    p { +"Select a robot" }

    select("form-control") {
        attributes["name"] = ActionField.ROBOT_MODEL.name

        RobotModel.values()
            .filter { model -> !game.players.mapNotNull { player -> player.robot?.model }.contains(model) }
            .forEach {
                option { +it.name }
            }
    }

    button(classes = "btn btn-primary", type = ButtonType.submit) { +"Join" }
}