package apoy2k.robby.routes

import apoy2k.robby.data.Storage
import apoy2k.robby.model.Board
import apoy2k.robby.model.Session
import apoy2k.robby.model.predef.board.generateChopShopBoard
import apoy2k.robby.templates.HomeTpl
import apoy2k.robby.templates.LayoutTpl
import apoy2k.robby.templates.renderBoard
import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.li
import kotlinx.html.ul
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

    get(Location.BOARDS_ROOT.path) {
        call.respondHtmlTemplate(LayoutTpl()) {
            content {
                div("row") {
                    div("col") {
                        ul {
                            li {
                                a(Location.BOARDS_VIEW.build(mapOf("id" to "chop-shop"))) {
                                    +"Chop Shop"
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    get(Location.BOARDS_VIEW.path) {
        val board = when (call.parameters["id"]) {
            "chop-shop" -> Board(generateChopShopBoard())
            else -> null
        }

        if (board == null) {
            call.respondRedirect(Location.BOARDS_ROOT.path)
            return@get
        }

        call.respondHtmlTemplate(LayoutTpl()) {
            content {
                div {
                    renderBoard(board)
                }
            }
        }
    }

    static("/static") {
        resources("static")
    }
}
