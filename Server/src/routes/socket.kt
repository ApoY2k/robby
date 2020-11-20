package apoy2k.robby.routes

import apoy2k.robby.data.Sockets
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
                application.environment.log.info(it.toString())
            }
        } finally {
            application.environment.log.info("Disconnecting websocket session")
            sockets.sessions.remove(this)
        }
    }
}
