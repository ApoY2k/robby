package apoy2k.robby.templates

import apoy2k.robby.*
import apoy2k.robby.engine.GameEngine
import apoy2k.robby.model.*
import kotlinx.html.*

fun HtmlBlockTag.renderGame(game: Game, session: Session?) {
    div(classes = "row") {
        attributes[ATTR_BIND] = View.GAME.toString()

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
