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
    private val fields: List<List<Field>>,
    private val session: Session?,
    private val currentRobot: Robot?,
    private val cards: List<MovementCard>,
) : Template<FlowContent> {
    override fun FlowContent.apply() {
        div {
            attributes["id"] = "gameview"

            div("row") {
                div("col") {
                    renderBoard(fields)
                }

                div("col-3") {
                    renderJoinForm(now, game, robots, session)
                    renderPlayers(robots, session)
                    renderGameState(now, game)
                }
            }

            if (currentRobot != null) {
                renderProfile(now, game, cards, currentRobot)
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
