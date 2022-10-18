package apoy2k.robby.templates

import apoy2k.robby.model.Game
import apoy2k.robby.model.Session
import kotlinx.html.*

fun HtmlBlockTag.renderPlayers(game: Game, session: Session?) {
    div("row mb-3") {
        div("col") {
            ul("list-group") {
                game.players.forEach {
                    val isSessionPlayer = it.session == session
                    li("list-group-item") {
                        if (it.ready) {
                            attributes["class"] += " text-success"
                        }

                        if (isSessionPlayer) {
                            strong { +it.name }
                        } else {
                            +it.name
                        }

                        +" (${it.robot.passedCheckpoints})"

                        br {}

                        it.robot.model.name
                    }
                }
            }
        }
    }
}
