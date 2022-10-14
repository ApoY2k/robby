package apoy2k.robby.templates

import apoy2k.robby.model.*
import kotlinx.html.*

fun HtmlBlockTag.renderProfile(game: Game, session: Session?) {
    val player = game.playerFor(session)
    val robot = player?.robot

    div("my-3") {
        if (player == null) {
            return@div
        }

        if (robot == null) {
            div("row") {
                div("col") {
                    p("alert alert-info") { +"You do not have a robot in the game. Specating only" }
                }
            }

            return@div
        }

        if (game.isFinished) {
            div("row") {
                div("col") {
                    p("alert alert-info") { +"Game is finished. Nothing else to do here" }
                }
            }

            return@div
        }

        div("row") {
            div("col") {
                if (game.state == GameState.PROGRAMMING_REGISTERS) {
                    if (player.robot?.poweredDown == true) {
                        div("row") {
                            div("col") {
                                p("alert alert-warning") { +"Robot is powered down" }
                            }
                        }
                    } else {
                        div("row row-cols-5") {
                            renderRegister(1, player)
                            renderRegister(2, player)
                            renderRegister(3, player)
                            renderRegister(4, player)
                            renderRegister(5, player)
                        }
                    }

                    if (player.drawnCards.isNotEmpty()) {
                        div("row mt-3") {
                            div("col") {
                                if (player.cardsConfirmed) {
                                    button(classes = "btn btn-danger") {
                                        attributes["data-action"] = ConfirmCardsAction().serializeForSocket()
                                        +"Revoke confirmation of cards"
                                    }
                                } else {
                                    if (robot.hasAllRegistersFilled()) {
                                        button(classes = "btn btn-primary") {
                                            attributes["data-action"] = ConfirmCardsAction().serializeForSocket()
                                            +"Confirm selected cards"
                                        }
                                    } else {
                                        p("alert alert-info") {
                                            +"Select a card for all registers to confirm"
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    p("alert alert-info") {
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
            div("col-3") {
                h4 { +robot.model.name }
                h5 { +"Damage buffer" }
                div("progress") {
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
                if (player.robot?.poweredDown == false && game.state == GameState.PROGRAMMING_REGISTERS) {
                    button(classes = "btn mt-3 btn-warning") {
                        attributes["data-toggle"] = "tooltip"
                        attributes["data-action"] = PowerDownAction().serializeForSocket()
                        attributes["title"] = "Powers down the robot, repairing some damage at the end of the round"
                        +"Toggle Power Down"
                    }
                }
                if (player.powerDownScheduled) {
                    p("alert mt-3 alert-warning") { +"Power Down scheduled" }
                }
            }
        }
    }
}

fun HtmlBlockTag.renderRegister(register: Int, player: Player) {
    val robot = player.robot ?: return
    val locked = player.cardsConfirmed || robot.isLocked(register)

    div("col pb-3") {
        if (locked) {
            attributes["class"] += " register-locked"
        }

        h5 { +"Register $register" }

        div("btn-group-vertical w-100") cards@{
            if (locked) {
                renderCard(register, robot.getRegister(register), locked = true, selected = true)
            } else {
                player.drawnCards.forEach {
                    val selected = robot.getRegister(register) == it
                    renderCard(register, it, locked = false, selected)
                }
            }
        }
    }
}

fun HtmlBlockTag.renderCard(register: Int, card: MovementCard?, locked: Boolean, selected: Boolean) {
    if (card == null) {
        return
    }

    val title = when (card.movement) {
        Movement.STRAIGHT -> "↑"
        Movement.STRAIGHT_2 -> "↑↑"
        Movement.STRAIGHT_3 -> "↑↑↑"
        Movement.TURN_LEFT -> "↰"
        Movement.TURN_RIGHT -> "↱"
        Movement.TURN_180 -> "⇅"
        Movement.BACKWARDS -> "↓"
        Movement.STAY -> " "
    }

    button(classes = "btn register-card", type = ButtonType.button) {
        if (selected) {
            attributes["class"] += " btn-success"
        } else {
            attributes["class"] += " btn-secondary"
        }

        if (!locked) {
            attributes["data-action"] = SelectCardAction(register.toString(), card.id).serializeForSocket()
        }

        +title

        span("card-priority") {
            +card.priority.toString()
        }
    }
}

fun HtmlBlockTag.renderDamageBuffer(robot: Robot, threshold: Int, bg: String, tooltip: String = "") {
    div("progress-bar bg-$bg") {
        attributes["style"] = "width: 10%;"

        if (tooltip.isNotBlank()) {
            attributes["data-bs-toggle"] = "tooltip"
            attributes["title"] = tooltip
        }

        if (robot.damage < threshold) {
            attributes["style"] += "opacity: 20%;"
        }
    }
}
