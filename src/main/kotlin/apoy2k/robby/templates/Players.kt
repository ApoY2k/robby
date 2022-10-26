package apoy2k.robby.templates

import apoy2k.robby.model.Robot
import apoy2k.robby.model.Session
import kotlinx.html.*

fun HtmlBlockTag.renderPlayers(
    robots: List<Robot>,
    session: Session?
) {
    div("row mb-3") {
        div("col") {
            ul("list-group") {
                robots.forEach {
                    val isSessionPlayer = it.sessionId == session?.id
                    li("list-group-item") {
                        if (it.ready) {
                            attributes["class"] += " text-success"
                        }

                        if (isSessionPlayer) {
                            strong { +it.name }
                        } else {
                            +it.name
                        }

                        +" (${it.passedCheckpoints})"

                        br {}

                        it.model.name
                    }
                }
            }
        }
    }
}
