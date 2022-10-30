package apoy2k.robby.templates

import apoy2k.robby.model.Game
import apoy2k.robby.model.Session
import io.ktor.server.html.*
import kotlinx.html.FlowContent
import kotlinx.html.b
import kotlinx.html.div
import kotlinx.html.p

class HomeTpl(
    private val games: List<Game>,
    private val session: Session?
) : Template<FlowContent> {
    override fun FlowContent.apply() {
        div("row") {
            div("col") {
                if (session?.isLoggedIn == true) {
                    p {
                        +"Welcome back "
                        b { +session.name }
                    }
                } else {
                    p { +"Welcome to robby, stranger!" }
                }
            }
        }
        insert(Lobby(session, games)) {}
    }
}
