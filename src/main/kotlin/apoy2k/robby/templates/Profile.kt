package apoy2k.robby.templates

import apoy2k.robby.model.*
import kotlinx.html.*

fun HtmlBlockTag.renderProfile(game: Game, session: Session?) {
    val player = game.playerFor(session)

    div("my-3") {
        if (player == null) {
            return@div
        }

        if (game.isFinished) {
            return@div
        }

        div("row") {
            div("col") {
                renderRegisterPanel(game, player)
            }
            div("col-3") {
                renderInfoPanel(game, player)
            }
        }
    }
}

fun HtmlBlockTag.renderRegisterPanel(game: Game, player: Player) {
    if (game.state == GameState.PROGRAMMING_REGISTERS) {
        if (player.robot.poweredDown) {
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

fun HtmlBlockTag.renderRegister(register: Int, player: Player) {
    val locked = player.ready || player.robot.isLocked(register)

    div("col pb-3") {
        if (locked) {
            attributes["class"] += " register-locked"
        }

        h5 { +"Register $register" }

        div("btn-group-vertical w-100") cards@{
            if (locked) {
                renderCard(register, player.robot.getRegister(register), locked = true, selected = true)
            } else {
                player.drawnCards.forEach {
                    val selected = player.robot.getRegister(register) == it
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

fun HtmlBlockTag.renderInfoPanel(game: Game, player: Player) {
    h4 { +player.robot.model.name }
    h5 { +"Damage buffer" }
    div("progress") {
        renderDamageBuffer(player.robot, 1, "secondary")
        renderDamageBuffer(player.robot, 2, "secondary")
        renderDamageBuffer(player.robot, 3, "secondary")
        renderDamageBuffer(player.robot, 4, "secondary")
        renderDamageBuffer(player.robot, 5, "warning", "LOCKS Register 5")
        renderDamageBuffer(player.robot, 6, "warning", "LOCKS Register 4")
        renderDamageBuffer(player.robot, 7, "warning", "LOCKS Register 3")
        renderDamageBuffer(player.robot, 8, "warning", "LOCKS Register 2")
        renderDamageBuffer(player.robot, 9, "warning", "LOCKS Register 1")
        renderDamageBuffer(player.robot, 10, "danger", "DESTROYED")
    }
    if (!player.robot.poweredDown && game.state == GameState.PROGRAMMING_REGISTERS) {
        button(classes = "btn mt-3 btn-warning") {
            attributes["data-toggle"] = "tooltip"
            attributes["data-action"] = TogglePowerDown().serializeForSocket()
            attributes["title"] = "Powers down the robot, repairing some damage at the end of the round"
            if (player.powerDownScheduled) {
                attributes["class"] += " btn-danger"
                +"Revoke Power Down"
            } else {
                +"Schedule Power Down"
            }
        }
    }
    if (game.state == GameState.PROGRAMMING_REGISTERS) {
        button(classes = "btn mt-3") {
            attributes["data-action"] = ToggleReady().serializeForSocket()
            if (player.ready) {
                attributes["class"] += " btn-danger"
                +"Revoke Ready"
            } else {
                attributes["class"] += " btn-success"
                +"Ready"
            }
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
