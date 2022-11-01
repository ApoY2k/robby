package apoy2k.robby.templates

import apoy2k.robby.model.Game
import apoy2k.robby.model.User
import apoy2k.robby.routes.Location
import io.ktor.server.html.*
import kotlinx.html.*
import java.time.Instant

class HomeTpl(
    private val now: Instant,
    private val games: List<Game>,
    private val user: User?,
) : Template<FlowContent> {
    override fun FlowContent.apply() {
        div("row") {
            div("col") {
                if (user != null) {
                    p {
                        +"Welcome back "
                        b { +user.name }
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
        insert(Lobby(now, user, games)) {}
    }
}
