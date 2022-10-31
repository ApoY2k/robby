package apoy2k.robby.templates

import apoy2k.robby.model.Game
import apoy2k.robby.model.Session
import apoy2k.robby.routes.Location
import io.ktor.server.html.*
import kotlinx.html.*

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
                        +"!"
                    }
                } else {
                    p { +"Welcome to robby, stranger!" }
                    p {
                        a(Location.AUTH.path) { +"Login" }
                        +" to start playing!"
                    }
                }
            }
        }
        insert(Lobby(session, games)) {}
    }
}
