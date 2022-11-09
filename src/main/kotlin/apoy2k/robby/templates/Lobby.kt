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
            // TODO Filter / pagination for game cards
            games.forEach {
                renderGame(now, user, it)
            }
        }
    }
}

fun FlowContent.renderGame(now: Instant, user: User?, game: Game) {
    val isFinished = game.isFinished(now)
    val hasStarted = game.hasStarted(now)

    div("col-3") {
        if (isFinished) {
            attributes["class"] += " opacity-50"
        }

        // TODO Highlight games the user is part of
        // TODO Show current / max number of robots in game

        a(Location.GAME_VIEW.build(mapOf("id" to game.id.toString())), classes = "card") {
            img("Game Preview", "/game/${game.id}/image", "card-img-top")
            div("card-body") {
                h5("card-title") {
                    +"Game #${game.id}"
                }
            }
            ul("list-group list-group-flush") {
                li("list-group-item") { +game.boardType.name }
                li("list-group-item") {
                    if (isFinished) {
                        +"Finished at ${game.finishedAt}"
                    } else if (hasStarted) {
                        +"Started at ${game.startedAt}"
                    } else {
                        +"Open to join"
                    }
                }
            }
        }
    }
}
