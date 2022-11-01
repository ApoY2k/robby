package apoy2k.robby.templates

import apoy2k.robby.model.Robot
import apoy2k.robby.model.User
import kotlinx.html.*

fun HtmlBlockTag.renderPlayers(
    robots: List<Robot>,
    user: User?
) {
    div("row mb-3") {
        div("col") {
            ul("list-group") {
                robots.forEach {
                    val isSessionPlayer = user != null && it.userId == user.id
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
