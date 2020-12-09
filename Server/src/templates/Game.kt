package apoy2k.robby.templates

import apoy2k.robby.ATTR_BIND
import apoy2k.robby.model.Game
import apoy2k.robby.model.Session
import apoy2k.robby.model.View
import kotlinx.html.HtmlBlockTag
import kotlinx.html.div

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
