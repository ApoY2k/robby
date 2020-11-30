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
                                attributes[ATTR_ACTION] =
                                    PlaceRobotCommand(it.id.toString(), RobotModel.ZIPPY.name).toString()
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
                    if (it.cardsConfirmed) {
                        attributes["class"] += " text-success"
                    }

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

fun HtmlBlockTag.renderCards(game: Game, session: Session?) {
    val player = game.playerFor(session) ?: return

    div(classes = "row mt-3") {
        attributes[ATTR_BIND] = VIEW_CARDS

        if (player.drawnCards.isEmpty()) {
            div(classes = "col") {
                button(classes = "btn btn-primary") {
                    attributes[ATTR_ACTION] = DrawCardsCommand().toString()
                    +"Draw new cards"
                }
            }
        }

        player.drawnCards.forEach { card ->
            val selectedOrder = player.selectedCards.indexOf(card) + 1

            div(classes = "col") {
                div(classes = "card") {
                    if (!player.cardsConfirmed) {
                        attributes[ATTR_ACTION] = SelectCardCommand(card.id.toString()).toString()
                    }

                    div(classes = "card-body") {
                        div(classes = "card-text") {
                            p {
                                +"${card.movement.name} (${card.priority})"
                            }

                            if (selectedOrder > 0) {
                                p {
                                    +"<selected as #$selectedOrder>"
                                }
                            }
                        }
                    }
                }
            }
        }

        if (player.selectedCards.count() == 3) {
            div(classes = "col") {
                button(classes = "btn") {
                    attributes[ATTR_ACTION] = ConfirmCardsCommand().toString()

                    if (player.cardsConfirmed) {
                        attributes["class"] += " btn-danger"
                        +"Revoke confirm"
                    } else {
                        attributes["class"] += " btn-primary"
                        +"Confirm cards"
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

    renderCards(game, session)
}
