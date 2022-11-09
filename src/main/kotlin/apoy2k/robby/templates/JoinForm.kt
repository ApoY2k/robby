package apoy2k.robby.templates

import apoy2k.robby.model.*
import kotlinx.html.*
import java.time.Instant

fun HtmlBlockTag.renderJoinForm(
    now: Instant,
    game: Game,
    robots: List<Robot>,
    user: User?,
) {
    div("row mb-3") {
        div("col") {
            div("card") {
                div("card-body") card@{
                    if (game.isFinished(now)) {
                        p("alert alert-info m-0") { +"Game has finished" }
                        return@card
                    }

                    if (user == null) {
                        p("alert alert-info m-0") { +"You are not logged in. Spectating only" }
                        return@card
                    }

                    form("form row") {
                        if (robots.any { it.userId == user.id }) {
                            if (game.state == GameState.PROGRAMMING_REGISTERS) {
                                attributes["data-action"] = Action.leaveGame().serializeForSocket()
                                button(classes = "btn btn-primary", type = ButtonType.submit) { +"Leave game" }
                            } else {
                                p("alert alert-info m-0") { +"Engine is running..." }
                            }
                            return@form
                        }

                        attributes["data-action"] = Action.joinGame().serializeForSocket()
                        p { +"Join this game" }
                        select("form-control mb-3") {
                            attributes["name"] = ActionField.ROBOT_MODEL.name

                            option {
                                attributes["selected"] = "selected"
                                attributes["disabled"] = "disabled"
                                +"Choose Robot Model"
                            }

                            RobotModel.values()
                                .filter { model -> !robots.map { it.model }.contains(model) }
                                .forEach {
                                    option { +it.name }
                                }
                        }
                        select("form-control mb-3") {
                            attributes["name"] = ActionField.ROBOT_FACING.name

                            option {
                                attributes["selected"] = "selected"
                                attributes["disabled"] = "disabled"
                                +"Choose Starting Orientation"
                            }

                            Direction.values()
                                .forEach {
                                    option { +it.name }
                                }
                        }
                        button(classes = "btn btn-primary", type = ButtonType.submit) { +"Join" }
                    }
                }
            }
        }
    }
}
