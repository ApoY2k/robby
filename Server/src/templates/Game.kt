package apoy2k.robby.templates

import apoy2k.robby.*
import apoy2k.robby.engine.Game
import apoy2k.robby.model.*
import kotlinx.html.*

fun HtmlBlockTag.renderBoard(game: Game, session: Session?) {
    div(classes = "row") {
        attributes[ATTR_BIND] = VIEW_BOARD

        div(classes = "col") {
            game.board.cells.forEach {
                div(classes = "row") {
                    it.forEach {
                        div(classes = "col btn btn-primary field") {
                            if (game.hasJoined(session)) {
                                val command = PlaceRobotCommand(it.id.toString(), RobotModel.ZIPPY.name)
                                attributes[ATTR_ACTION] = command.toString()
                            }

                            +(it.robot?.model?.name ?: "<Place>")
                        }
                    }
                }
            }
        }
    }
}

fun HtmlBlockTag.renderPlayers(game: Game, session: Session?) {
    div {
        attributes[ATTR_BIND] = VIEW_PLAYERS

        game.players.forEach {
            val isSessionPlayer = it.session == session

            div(classes = "row") {
                div(classes = "col") {
                    if (isSessionPlayer) {
                        strong {
                            +it.name
                        }
                    } else {
                        +it.name
                    }
                }
            }
        }
    }
}

fun HtmlBlockTag.renderJoinForm(game: Game, session: Session?) {
    div(classes = "row") {
        attributes[ATTR_BIND] = VIEW_JOIN_FORM;

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
                        input(classes = "form-control", name = CommandField.PLAYER_NAME.name)
                    }

                    button(classes = "btn btn-primary", type = ButtonType.submit) {
                        +"Join"
                    }
                }
            }
        }
    }
}

fun HtmlBlockTag.renderGame(game: Game, session: Session?) {
    div(classes = "row") {
        attributes[ATTR_BIND] = VIEW_GAME

        div(classes = "col") {
            renderBoard(game, session)
        }
        div(classes = "col-2") {
            renderPlayers(game, session)
            renderJoinForm(game, session)
        }
    }
    div(classes = "row") {
        div(classes = "col") {

        }
    }
}
