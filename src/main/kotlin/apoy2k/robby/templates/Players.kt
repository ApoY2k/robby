package apoy2k.robby.templates

import apoy2k.robby.model.Game
import apoy2k.robby.model.Session
import kotlinx.html.HtmlBlockTag
import kotlinx.html.div
import kotlinx.html.strong

fun HtmlBlockTag.renderPlayers(game: Game, session: Session?) {
    div {
        game.players.forEach {
            val isSessionPlayer = it.session == session

            div("row") {
                div("col") {
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

                    +" (${it.robot?.passedCheckpoints})"
                }
            }
        }
    }
}
