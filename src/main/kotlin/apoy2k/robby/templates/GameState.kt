package apoy2k.robby.templates

import apoy2k.robby.model.Game
import apoy2k.robby.model.GameState
import kotlinx.html.HtmlBlockTag
import kotlinx.html.UL
import kotlinx.html.li
import kotlinx.html.ul
import java.time.Instant

fun HtmlBlockTag.renderGameState(now: Instant, game: Game) {
    if (game.isFinished(now) || game.state == GameState.PROGRAMMING_REGISTERS) {
        return
    }

    ul("list-group mb-3") {
        renderRegister(game, 1)
        renderRegister(game, 2)
        renderRegister(game, 3)
        renderRegister(game, 4)
        renderRegister(game, 5)
    }
}

fun UL.renderRegister(game: Game, register: Int) {
    val isActive = register == game.currentRegister

    li("list-group-item") {
        if (isActive) {
            attributes["class"] += " active"
        }
        +"Register #$register"
    }

    if (isActive) {
        renderState("Executing Registers", game.state == GameState.EXECUTING_REGISTERS)
        renderState("Moving Fast Board Elements", game.state == GameState.MOVE_BARD_ELEMENTS_2)
        renderState("Moving Slow Board Elements", game.state == GameState.MOVE_BARD_ELEMENTS_1)
        renderState("Fire Double Lasers", game.state == GameState.FIRE_LASERS_2)
        renderState("Fire Single Lasers", game.state == GameState.FIRE_LASERS_1)
        renderState("Touching Checkpoints", game.state == GameState.CHECKPOINTS)
        renderState("Repair & Powerups", game.state == GameState.REPAIR_POWERUPS)
    }
}

fun UL.renderState(name: String, isActive: Boolean) {
    li("list-group-item ps-4") {
        if (isActive) {
            attributes["class"] += " bg-primary bg-opacity-50"
        }
        +name
    }
}
