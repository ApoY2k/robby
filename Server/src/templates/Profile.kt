package apoy2k.robby.templates

import apoy2k.robby.ATTR_ACTION
import apoy2k.robby.ATTR_BIND
import apoy2k.robby.model.*
import kotlinx.html.*

fun HtmlBlockTag.renderProfile(game: Game, session: Session?) {
    val player = game.playerFor(session)
    val robot = player?.robot

    div(classes = "my-3") {
        attributes[ATTR_BIND] = View.PROFILE.toString()

        if (player == null) {
            return@div
        }

        if (robot == null) {
            div(classes = "row") {
                div(classes = "col") {
                    p(classes = "alert alert-info") {
                        +"You do not have a robot in the game. Specating only"
                    }
                }
            }

            return@div
        }

        div(classes = "row") {
            div(classes = "col") {
                div(classes = "row row-cols-5") {
                    renderRegister(1, player, game.state == GameState.EXECUTING_REGISTER_1)
                    renderRegister(2, player, game.state == GameState.EXECUTING_REGISTER_2)
                    renderRegister(3, player, game.state == GameState.EXECUTING_REGISTER_3)
                    renderRegister(4, player, game.state == GameState.EXECUTING_REGISTER_4)
                    renderRegister(5, player, game.state == GameState.EXECUTING_REGISTER_5)
                }
            }
            div(classes = "col-3") {
                h4 {
                    +robot.model.name
                }
                div(classes = "progress") {
                    div(classes = "progress-bar w-50 bg-danger")
                    div(classes = "progress-bar w-50")
                }
            }
        }

        div(classes = "row mt-3") {
            if (player.drawnCards.isNotEmpty()) {
                div(classes = "col") {
                    if (player.cardsConfirmed) {
                        button(classes = "btn btn-danger") {
                            attributes[ATTR_ACTION] = ConfirmCardsAction().toString()
                            +"Revoke confirmation of cards"
                        }
                    } else {
                        if (robot.hasAllRegistersFilled()) {
                            button(classes = "btn btn-primary") {
                                attributes[ATTR_ACTION] = ConfirmCardsAction().toString()
                                +"Confirm selected cards"
                            }
                        } else {
                            p(classes = "alert alert-info") {
                                +"Select a card for all registers to confirm"
                            }
                        }
                    }
                }
            }
        }
    }
}

fun HtmlBlockTag.renderRegister(register: Int, player: Player, isExecuting: Boolean) {
    val robot = player.robot ?: return

    div(classes = "col") {
        if (isExecuting) {
            attributes["class"] += " shadow"
        }

        h5 {
            +"Register $register"
        }

        div(classes = "btn-group-vertical w-100") {
            player.drawnCards.forEach {
                renderCard(
                    register, it, player.cardsConfirmed, robot.getRegister(register) == it
                )
            }
        }
    }
}

fun HtmlBlockTag.renderCard(register: Int, card: MovementCard, locked: Boolean, selected: Boolean) {
    val title = when (card.movement) {
        Movement.STRAIGHT -> "↑"
        Movement.STRAIGHT_2 -> "↑↑"
        Movement.STRAIGHT_3 -> "↑↑↑"
        Movement.TURN_LEFT -> "↰"
        Movement.TURN_RIGHT -> "↱"
        Movement.TURN_180 -> "⇅"
        Movement.BACKWARDS -> "↓"
    }

    button(classes = "btn register-card", type = ButtonType.button) {
        if (selected) {
            attributes["class"] += " btn-success"
        } else {
            attributes["class"] += " btn-secondary"
        }

        if (!locked) {
            attributes[ATTR_ACTION] = SelectCardAction(register.toString(), card.id.toString()).toString()
        }

        +title

        span(classes = "card-priority") {
            +card.priority.toString()
        }
    }
}
