package apoy2k.robby.routes

import apoy2k.robby.engine.Engine
import apoy2k.robby.exceptions.UnknownCommandException
import apoy2k.robby.model.Command
import apoy2k.robby.model.Session
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.sessions.*
import io.ktor.websocket.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import org.slf4j.LoggerFactory

@OptIn(ExperimentalCoroutinesApi::class)
fun Route.socket(engine: Engine) {
    val logger = LoggerFactory.getLogger("routes/socket")

    webSocket("/ws") {
        val session = call.sessions.get<Session>()
        if (session == null) {
            logger.warn("No session associated with websocket connection")
            return@webSocket
        }

        logger.debug("Adding WebSocketSession to HttpSession [$session]");
        if (engine.sessions.containsKey(session)) {
            engine.sessions[session]?.add(this)
        } else {
            engine.sessions[session] = mutableSetOf(this)
        }

        try {
            incoming.consumeEach {
                when (it) {
                    is Frame.Text -> {
                        val data = it.readText()
                        try {
                            val command = Command.fromString(data)
                            engine.perform(listOf(command), session)
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
            logger.debug("Removing WebSocketSession from HttpSession [$session]")
            engine.sessions[session]?.remove(this)
        }
    }
}
