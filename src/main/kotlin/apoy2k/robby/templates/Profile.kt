package apoy2k.robby.templates

import apoy2k.robby.model.*
import kotlinx.html.*
import java.time.Instant

fun HtmlBlockTag.renderProfile(
    now: Instant,
    game: Game,
    sessionRobot: Robot,
    robotCards: List<MovementCard>,
) {
    div("my-3") {
        if (game.isFinished(now)) {
            return@div
        }

        div("row") {
            div("col") {
                renderRegisterPanel(game, sessionRobot, robotCards)
            }
            div("col-3") {
                renderInfoPanel(game, sessionRobot)
            }
        }
    }
}

fun HtmlBlockTag.renderRegisterPanel(
    game: Game,
    sessionRobot: Robot,
    robotCards: List<MovementCard>,
) {
    if (sessionRobot.poweredDown) {
        div("row") {
            div("col") {
                p("alert alert-warning") { +"Robot is powered down" }
            }
        }
    } else {
        div("row row-cols-5") {
            renderRegister(game, sessionRobot, robotCards, 1)
            renderRegister(game, sessionRobot, robotCards, 2)
            renderRegister(game, sessionRobot, robotCards, 3)
            renderRegister(game, sessionRobot, robotCards, 4)
            renderRegister(game, sessionRobot, robotCards, 5)
        }
    }
}

fun HtmlBlockTag.renderRegister(
    game: Game,
    sessionRobot: Robot,
    robotCards: List<MovementCard>,
    register: Int,
) {
    val isActive = game.state != GameState.PROGRAMMING_REGISTERS && game.currentRegister == register
    val locked = sessionRobot.ready || sessionRobot.isLocked(register)

    div("col pb-3 rounded") {
        if (locked && !isActive) {
            attributes["class"] += " register-locked"
        }

        if (isActive) {
            attributes["class"] += " border border-primary shadow"
        }

        h5 { +"Register $register" }

        div("btn-group-vertical w-100") cards@{
            if (locked) {
                renderCard(register, robotCards[register], locked = true, selected = true)
            } else {
                robotCards.forEach {
                    val selected = robotCards[register] == it
                    renderCard(register, it, locked = false, selected)
                }
            }
        }
    }
}

fun HtmlBlockTag.renderCard(
    register: Int,
    card: MovementCard?,
    locked: Boolean,
    selected: Boolean
) {
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
            attributes["data-action"] = Action.selectCard(register, card.id).serializeForSocket()
        }

        +title

        span("card-priority") {
            +card.priority.toString()
        }
    }
}

fun HtmlBlockTag.renderInfoPanel(
    game: Game,
    robot: Robot,
) {
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
    if (!robot.poweredDown && game.state == GameState.PROGRAMMING_REGISTERS) {
        button(classes = "btn mt-3 btn-warning") {
            attributes["data-toggle"] = "tooltip"
            attributes["data-action"] = Action.togglePowerDown().serializeForSocket()
            attributes["title"] = "Powers down the robot, repairing some damage at the end of the round"
            if (robot.powerDownScheduled) {
                attributes["class"] += " btn-danger"
                +"Revoke Power Down"
            } else {
                +"Schedule Power Down"
            }
        }
    }
    if (game.state == GameState.PROGRAMMING_REGISTERS) {
        button(classes = "btn mt-3") {
            attributes["data-action"] = Action.toggleReady().serializeForSocket()
            if (robot.ready) {
                attributes["class"] += " btn-danger"
                +"Revoke Ready"
            } else {
                attributes["class"] += " btn-success"
                +"Ready"
            }
        }
    }
}

fun HtmlBlockTag.renderDamageBuffer(
    robot: Robot,
    threshold: Int,
    bg: String,
    tooltip: String = ""
) {
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
