package apoy2k.robby.templates

import apoy2k.robby.model.Game
import apoy2k.robby.model.Session
import io.ktor.server.html.*
import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.script
import kotlin.collections.set

class GameTpl(val game: Game, val session: Session?) : Template<FlowContent> {
    override fun FlowContent.apply() {
        div {
            attributes["id"] = "gameview"

            div("row") {
                div("col") {
                    renderBoard(game.board)
                }

                div("col-2") {
                    renderPlayers(game, session)
                    renderJoinForm(game, session)
                }
            }

            renderProfile(game, session)

            // Add a javscript marker so the websocket connection can be established
            script("text/javascript") {
                +"const connectWs = true;"
            }
        }
    }
}
