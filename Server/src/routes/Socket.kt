package apoy2k.robby.routes

import apoy2k.robby.engine.WebSocketHandler
import apoy2k.robby.model.Action
import apoy2k.robby.model.LeaveGameAction
import apoy2k.robby.model.Session
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.sessions.*
import io.ktor.websocket.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach
import org.slf4j.LoggerFactory

@ExperimentalCoroutinesApi
fun Route.socket(webSocketHandler: WebSocketHandler, actions: SendChannel<Action>) {
    val logger = LoggerFactory.getLogger("routes/socket")

    webSocket("/ws") {
        val session = call.sessions.get<Session>()
        if (session == null) {
            logger.error("No session associated with websocket connection")
            return@webSocket
        }

        webSocketHandler.addSession(session, this)

        try {
            incoming.consumeEach {
                when (it) {
                    is Frame.Text -> {
                        try {
                            val data = it.readText()
                            val action = Action.fromString(data)
                            action.session = session
                            actions.send(action)
                        } catch (err: Throwable) {
                            logger.error(err.message, err)
                        }
                    }
                    else -> {
                        val data = it.readBytes().toString()
                        logger.warn("Unknown socket message [$data]")
                    }
                }
            }
        } finally {
            webSocketHandler.removeSession(session, this)
        }
    }
}
