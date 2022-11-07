package apoy2k.robby.templates

import apoy2k.robby.model.Game
import apoy2k.robby.model.User
import apoy2k.robby.routes.Location
import io.ktor.server.html.*
import kotlinx.html.*
import java.time.Instant

class Lobby(
    private val now: Instant,
    private val user: User?,
    private val games: List<Game>
) : Template<FlowContent> {
    override fun FlowContent.apply() {
        div("row") {
            div("col") {
                h2 { +"Available games" }
            }
            if (user != null) {
                div("col-2") {
                    renderCreateGameButton()
                }
            }
        }
        div("row") {
            games.forEach {
                div("col-3") {
                    a(Location.GAME_VIEW.build(mapOf("id" to it.id.toString())), classes = "card") {
                        img("Game Preview", "/game/${it.id}/image", "card-img-top")
                        div("card-body") {
                            h5("card-title") {
                                +"Game #${it.id}"
                            }
                        }
                        ul("list-group list-group-flush") {
                            li("list-group-item") { +it.boardType.name }
                            li("list-group-item") {
                                if (it.isFinished(now)) {
                                    +"Finished at ${it.finishedAt}"
                                } else if (it.hasStarted(now)) {
                                    +"Started at ${it.startedAt}"
                                } else {
                                    +"Open to join"
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
