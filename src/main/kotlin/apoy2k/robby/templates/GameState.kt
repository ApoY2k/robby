package apoy2k.robby.templates

import apoy2k.robby.model.Game
import apoy2k.robby.model.GameState
import kotlinx.html.HtmlBlockTag
import kotlinx.html.UL
import kotlinx.html.li
import kotlinx.html.ul

fun HtmlBlockTag.renderGameState(game: Game) {
    if (game.state == GameState.PROGRAMMING_REGISTERS) {
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
        renderState("Executing Registers", game.state == GameState.EXECUTING_REGISTER)
        renderState("Moving Board Elements", game.state == GameState.MOVE_BARD_ELEMENTS)
        renderState("Fire Lasers", game.state == GameState.FIRE_LASERS)
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
