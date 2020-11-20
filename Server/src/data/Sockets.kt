package apoy2k.robby.data

import io.ktor.http.cio.websocket.*
import io.ktor.util.collections.*
import io.ktor.websocket.*

class Sockets {
    val sessions = HashSet<WebSocketServerSession>()

    fun add(session: WebSocketServerSession) {
        sessions.add(session)
    }

    suspend fun send(message: String) {
        sessions.forEach {
            it.send(Frame.Text(message))
        }
    }
}
