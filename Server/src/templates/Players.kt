package apoy2k.robby.templates

import apoy2k.robby.ATTR_BIND
import apoy2k.robby.VIEW_PLAYERS
import apoy2k.robby.engine.Game
import apoy2k.robby.model.Session
import kotlinx.html.HtmlBlockTag
import kotlinx.html.div
import kotlinx.html.strong

fun HtmlBlockTag.renderPlayers(game: Game, session: Session?) {
    div {
        attributes[ATTR_BIND] = VIEW_PLAYERS

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
