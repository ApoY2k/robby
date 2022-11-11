package apoy2k.robby.templates

import apoy2k.robby.model.Game
import apoy2k.robby.model.Robot
import apoy2k.robby.model.User
import apoy2k.robby.routes.Location
import io.ktor.server.html.*
import kotlinx.html.*
import java.time.Instant

class Lobby(
    private val now: Instant,
    private val user: User?,
    private val games: List<Game>,
    private val robots: List<Robot>,
) : Template<FlowContent> {
    override fun FlowContent.apply() {
        div("row mb-3") {
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
            games.forEach { game ->
                renderGame(now, user, robots.filter { it.gameId == game.id }, game)
            }
        }
    }
}

fun FlowContent.renderGame(now: Instant, user: User?, robots: List<Robot>, game: Game) {
    val isFinished = game.isFinished(now)
    val hasStarted = game.hasStarted(now)
    val hasJoined = robots.any { it.userId == user?.id }

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
                    if (hasJoined) {
                        attributes["class"] += " text-success"
                    }

                    if (isFinished) {
                        +"Finished"
                    } else if (hasStarted) {
                        +"In progress"
                    } else {
                        if (hasJoined) {
                            +"Joined"
                        } else {
                            +"Open to join"
                        }
                    }

                    +" (${robots.count()}/${game.maxRobots})"
                }
            }
        }
    }
}
