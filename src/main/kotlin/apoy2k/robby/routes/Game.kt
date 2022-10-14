package apoy2k.robby.routes

import apoy2k.robby.data.Storage
import apoy2k.robby.engine.ViewUpdateRouter
import apoy2k.robby.model.Action
import apoy2k.robby.model.Session
import apoy2k.robby.templates.GameTpl
import apoy2k.robby.templates.LayoutTpl
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.MutableSharedFlow
import org.slf4j.LoggerFactory
import java.io.File

fun Route.game(actions: MutableSharedFlow<Action>, viewUpdateRouter: ViewUpdateRouter, storage: Storage) {
    val logger = LoggerFactory.getLogger("${this.javaClass.name}.game")

    post(Location.GAME_ROOT.path) {
        val game = storage.createGame()
        call.respondRedirect(Location.ROOT.path)
    }

    route(Location.GAME_VIEW.path) {
        get {
            val game = storage.findGame(call.parameters["id"])
            if (game == null) {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }

            call.respondHtmlTemplate(LayoutTpl()) {
                content {
                    insert(GameTpl(game, call.sessions.get<Session>())) {}
                }
            }
        }

        webSocket("/ws") {
            val session = call.sessions.get<Session>()
            if (session == null) {
                logger.error("No session associated with WebSocket connection")
                call.respond(HttpStatusCode.Forbidden)
                return@webSocket
            }

            val game = storage.findGame(call.parameters["id"])
            if (game == null) {
                logger.error("No game found")
                return@webSocket
            }

            viewUpdateRouter.addSession(game, session, this)

            // This will block the thread while listening for incoming messages
            incoming.consumeEach { frame ->
                when (frame) {
                    is Frame.Text -> {
                        try {
                            val data = frame.readText()
                            logger.debug("Received [$data] from $session on $game")
                            val action = Action.deserializeFromSocket(game, data)
                            action.session = session
                            actions.emit(action)
                        } catch (err: Throwable) {
                            logger.error(err.message, err)
                        }
                    }
                    else -> {
                        val data = frame.readBytes().toString()
                        logger.warn("Unknown socket message [$data]")
                    }
                }
            }

            // At this point, the websocket connection was aborted (by either client or server)
            viewUpdateRouter.removeSession(game, session, this)
        }
    }

    get(Location.GAME_IMAGE.path) {
        // TODO: generate image preview of game state
        val dummy = javaClass.getResource("/assets/dummy-board-preview.png").toURI()
        call.respondFile(File(dummy))
    }
}