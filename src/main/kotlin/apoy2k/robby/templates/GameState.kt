package apoy2k.robby.templates

import apoy2k.robby.model.Game
import apoy2k.robby.model.GameState
import kotlinx.html.HtmlBlockTag
import kotlinx.html.UL
import kotlinx.html.li
import kotlinx.html.ul

fun HtmlBlockTag.renderGameState(game: Game) {
    ul("list-group mb-3") {
        render("Programming Registers", game.state == GameState.PROGRAMMING_REGISTERS)
        render("Executing Register #1", game.state == GameState.EXECUTING_REGISTER_1)
        render("Executing Register #2", game.state == GameState.EXECUTING_REGISTER_2)
        render("Executing Register #3", game.state == GameState.EXECUTING_REGISTER_3)
        render("Executing Register #4", game.state == GameState.EXECUTING_REGISTER_4)
        render("Executing Register #5", game.state == GameState.EXECUTING_REGISTER_5)
        render("Moving Board Elements", game.state == GameState.MOVE_BARD_ELEMENTS)
        render("Fire Lasers", game.state == GameState.FIRE_LASERS)
        render("Touching Checkpoints", game.state == GameState.CHECKPOINTS)
        render("Repair & Powerups", game.state == GameState.REPAIR_POWERUPS)
    }
}

fun UL.render(name: String, isActive: Boolean) {
    li("list-group-item") {
        if (isActive) {
            attributes["class"] += " active"
        }
        +name
    }
}
