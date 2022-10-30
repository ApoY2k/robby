package apoy2k.robby.templates

import apoy2k.robby.model.*
import io.ktor.server.html.*
import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.script
import kotlinx.html.unsafe
import java.time.Instant
import kotlin.collections.set

class GameTpl(
    private val now: Instant,
    private val game: Game,
    private val robots: List<Robot>,
    private val board: List<List<Field>>,
    private val session: Session?,
    private val sessionRobot: Robot?,
    private val robotCards: List<MovementCard>,
) : Template<FlowContent> {
    override fun FlowContent.apply() {
        div {
            attributes["id"] = "gameview"

            div("row") {
                div("col") {
                    renderBoard(board, robots)
                }

                div("col-3") {
                    renderJoinForm(now, game, robots, session)
                    renderPlayers(robots, session)
                    renderGameState(now, game)
                }
            }

            if (sessionRobot != null) {
                renderProfile(now, game, sessionRobot, robotCards)
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
