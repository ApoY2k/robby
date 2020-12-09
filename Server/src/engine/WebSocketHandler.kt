package apoy2k.robby.engine

import apoy2k.robby.model.Session
import apoy2k.robby.model.ViewUpdate
import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import org.slf4j.LoggerFactory

/**
 * Manages mapping between HTTP and WebSocket sessions
 */
class WebSocketHandler(private val actions: Channel<ViewUpdate>) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Map HTTP Sessions to WebSocketSessions.
     * Each HTTP Session can have multiple WebSocketSessions associated with it
     */
    val sessions = HashMap<Session, MutableSet<WebSocketServerSession>>()

    /**
     * Connect this WebSocketHandler to a command channel that can be used to send
     * messages to a set of recipients, identified via their session
     */
    @ExperimentalCoroutinesApi
    suspend fun connect() {
        actions.consumeEach { viewUpdate ->
            try {
                sessions
                    .filter { (k, _) ->
                        viewUpdate.recipients.isEmpty() || viewUpdate.recipients.contains(k)
                    }
                    .forEach { (k, v) ->
                        logger.debug("Sending ViewUpdate [$viewUpdate] to session [$k] (${v.count()} sockets)")
                        v.forEach { it.send(Frame.Text(viewUpdate.toString())) }
                    }
            } catch (err: Throwable) {
                logger.error("Error sending VieWUpdate [$viewUpdate]: [${err.message}]", err)
            }
        }
    }
}
