package apoy2k.robby.templates

import apoy2k.robby.engine.Board
import apoy2k.robby.model.Game
import apoy2k.robby.model.MovementCard
import apoy2k.robby.model.Robot
import apoy2k.robby.model.User
import io.ktor.server.html.*
import kotlinx.html.*
import java.time.Instant
import kotlin.collections.set

class GameTpl(
    private val now: Instant,
    private val game: Game,
    private val robots: List<Robot>,
    private val board: Board,
    private val user: User?,
    private val currentRobot: Robot?,
    private val robotCards: List<MovementCard>,
) : Template<FlowContent> {
    override fun FlowContent.apply() {
        div("row") {
            div("col") {
                h1 { +"Game #${game.id}" }
            }
        }
        div {
            attributes["id"] = "gameview"

            div("row") {
                div("col") {
                    renderBoard(board, robots)
                }

                div("col-3") {
                    renderJoinForm(now, game, robots, user)
                    renderPlayers(robots, user)
                    renderGameState(now, game)
                }
            }

            if (currentRobot != null) {
                renderProfile(now, game, currentRobot, robotCards)
            }

            // Add a javscript marker so the websocket connection can be established
            script("text/javascript") {
                unsafe {
                    +"window.connectWs = true;"
                }
            }
        }
    }
}
