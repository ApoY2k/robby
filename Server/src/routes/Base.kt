package apoy2k.robby.routes

import apoy2k.robby.data.Storage
import apoy2k.robby.model.Session
import apoy2k.robby.templates.HomeTpl
import apoy2k.robby.templates.LayoutTpl
import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.content.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import org.slf4j.LoggerFactory

fun Route.base(storage: Storage) {
    val logger = LoggerFactory.getLogger("${this.javaClass.name}.base")

    get("/") {
        call.respondHtmlTemplate(LayoutTpl()) {
            content {
                insert(HomeTpl(storage, call.sessions.get())) {}
            }
        }
    }

    post(Location.SET_USERNAME.path) {
        val form = call.receiveParameters()
        val name = form["username"] ?: ""
        val session = call.sessions.get<Session>()
        logger.debug("Saving username [$name] for $session")
        call.sessions.set(session?.copy(name = name))
        call.respondRedirect("/")
    }

    static("/static") {
        resources("static")
    }
}
