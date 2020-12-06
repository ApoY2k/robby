package apoy2k.robby.templates

import apoy2k.robby.ATTR_BIND
import apoy2k.robby.model.Game
import apoy2k.robby.model.Session
import apoy2k.robby.model.View
import kotlinx.html.HtmlBlockTag
import kotlinx.html.div
import kotlinx.html.strong

fun HtmlBlockTag.renderPlayers(game: Game, session: Session?) {
    div {
        attributes[ATTR_BIND] = View.PLAYERS.toString()

        game.players.forEach {
            val isSessionPlayer = it.session == session

            div(classes = "row") {
                div(classes = "col") {
                    if (it.cardsConfirmed) {
                        attributes["class"] += " text-success"
                    }

                    if (isSessionPlayer) {
                        strong {
                            +it.name
                        }
                    } else {
                        +it.name
                    }
                }
            }
        }
    }
}
