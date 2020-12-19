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
                if (game.state == GameState.PROGRAMMING_REGISTERS) {
                    div(classes = "row row-cols-5") {
                        renderRegister(1, player)
                        renderRegister(2, player)
                        renderRegister(3, player)
                        renderRegister(4, player)
                        renderRegister(5, player)
                    }
                    if (player.drawnCards.isNotEmpty()) {
                        div(classes = "row mt-3") {
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
                } else {
                    p(classes = "alert alert-info") {
                        when (game.state) {
                            GameState.EXECUTING_REGISTER_1 -> +"Executing register 1"
                            GameState.EXECUTING_REGISTER_2 -> +"Executing register 2"
                            GameState.EXECUTING_REGISTER_3 -> +"Executing register 3"
                            GameState.EXECUTING_REGISTER_4 -> +"Executing register 4"
                            GameState.EXECUTING_REGISTER_5 -> +"Executing register 5"
                            GameState.MOVE_BARD_ELEMENTS -> +"Moving board elements"
                            GameState.FIRE_LASERS -> +"Firing lasers"
                            GameState.CHECKPOINTS -> +"Touching checkpoints"
                            GameState.REPAIR_POWERUPS -> +"Repair & powerups"
                            else -> +"Waiting"
                        }
                    }
                }
            }
            div(classes = "col-3") {
                h4 {
                    +robot.model.name
                }
                h5 {
                    +"Damage buffer"
                }
                div(classes = "progress") {
                    renderDamageBuffer(robot, 1, "secondary")
                    renderDamageBuffer(robot, 2, "secondary")
                    renderDamageBuffer(robot, 3, "secondary")
                    renderDamageBuffer(robot, 4, "secondary")
                    renderDamageBuffer(robot, 5, "warning", "LOCKS Register 5")
                    renderDamageBuffer(robot, 6, "warning", "LOCKS Register 4")
                    renderDamageBuffer(robot, 7, "warning", "LOCKS Register 3")
                    renderDamageBuffer(robot, 8, "warning", "LOCKS Register 2")
                    renderDamageBuffer(robot, 9, "warning", "LOCKS Register 1")
                    renderDamageBuffer(robot, 10, "danger", "DESTROYED")
                }
                h5(classes = "mt-3") {
                    +"Modifications"
                }
                hr {}
                button(classes = "btn btn-warning") {
                    attributes["data-toggle"] = "tooltip"
                    attributes["title"] = "Powers down the robot, repairing some damage at the end of the round"

                    +"Power down"
                }
            }
        }
    }
}

fun HtmlBlockTag.renderRegister(register: Int, player: Player) {
    val robot = player.robot ?: return

    div(classes = "col") {
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

fun HtmlBlockTag.renderDamageBuffer(robot: Robot, threshold: Int, bg: String, tooltip: String = "") {
    div(classes = "progress-bar bg-$bg") {
        attributes["style"] = "width: 10%;"

        if (tooltip.isNotBlank()) {
            attributes["data-toggle"] = "tooltip"
            attributes["title"] = tooltip
        }

        if (robot.damage < threshold) {
            attributes["style"] += "opacity: 50%;"
        }
    }
}
