package apoy2k.robby.routes

import apoy2k.robby.model.Session
import apoy2k.robby.templates.LayoutTpl
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.*
import org.slf4j.LoggerFactory

fun Route.auth() {
    val logger = LoggerFactory.getLogger("${this.javaClass.name}.base")

    get(Location.AUTH.path) {
        val session = call.sessions.get<Session>()

        call.respondHtmlTemplate(LayoutTpl(session)) {
            content {
                div("row") {
                    div("col") {
                        p { +"Choose a username to use while playing and then join a game or create a new one" }
                        val action = Location.AUTH.build()
                        form(action, method = FormMethod.post) {
                            div("row") {
                                div("col") {
                                    div("input-group") {
                                        span("input-group-text") { +"Username" }
                                        input(InputType.text, name = "username", classes = "form-control")
                                    }
                                }
                                div("col") {
                                    button(classes = "btn btn-primary") { +"Join" }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    post(Location.AUTH.path) {
        val form = call.receiveParameters()
        val name = form["username"] ?: ""
        if (name.isBlank()) {
            throw Exception("Username must not be blank")
        }

        val session = call.sessions.get<Session>()
        logger.debug("Saving username [$name] for $session")
        call.sessions.set(
            session?.copy(name = name)
        )
        call.respondRedirect(Location.ROOT.path)
    }
}
