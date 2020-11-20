package apoy2k.robby.routes

import apoy2k.robby.data.Sockets
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.consumeEach

@OptIn(ExperimentalCoroutinesApi::class)
fun Route.socket(sockets: Sockets) {
    webSocket("/ws") {
        sockets.sessions.add(this)

        try {
            incoming.consumeEach {
                when (it) {
                    is Frame.Text -> application.environment.log.info(it.readText())
                    else -> application.environment.log.info(it.readBytes().toString())
                }
            }
        } finally {
            application.environment.log.info("Disconnecting websocket session")
            sockets.sessions.remove(this)
        }
    }
}
