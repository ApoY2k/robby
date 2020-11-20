package apoy2k.robby.routes

import apoy2k.robby.engine.Engine
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import org.slf4j.LoggerFactory

@OptIn(ExperimentalCoroutinesApi::class)
fun Route.socket(engine: Engine) {
    val logger = LoggerFactory.getLogger("routes/socket")

    webSocket("/ws") {
        engine.sessions.add(this)

        try {
            incoming.consumeEach {
                when (it) {
                    is Frame.Text -> {
                        val data = it.readText()
                        logger.debug("Received [$data]")
                        engine.perform(data)
                    }
                    else -> {
                        val data = it.readBytes().toString()
                        logger.warn("Unknown socket data [$data]")
                    }
                }
            }
        } finally {
            logger.info("Disconnecting websocket session")
            engine.sessions.remove(this)
        }
    }
}
