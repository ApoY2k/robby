package apoy2k.robby.engine

import apoy2k.robby.model.Action
import apoy2k.robby.model.Session
import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach
import org.slf4j.LoggerFactory

/**
 * Manages mapping between HTTP and WebSocket sessions
 */
class WebSocketHandler(private val viewUpdates: ReceiveChannel<Unit>, private val actions: SendChannel<Action>) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Map HTTP Sessions to WebSocketSessions.
     * Each HTTP Session can have multiple WebSocketSessions associated with it
     */
    private val sessions = HashMap<Session, MutableSet<WebSocketServerSession>>()

    /**
     * Connect this WebSocketHandler to a command channel that can be used to send
     * messages to a set of recipients, identified via their session
     */
    @ExperimentalCoroutinesApi
    suspend fun connect() {
        viewUpdates.consumeEach { viewUpdate ->
            try {
                sessions
                    .forEach { (k, v) ->
                        logger.debug("Sending ViewUpdate [$viewUpdate] to session [$k] (${v.count()} sockets)")
                        v.forEach { it.send(Frame.Text(viewUpdate.toString())) }
                    }
            } catch (err: Throwable) {
                logger.error("Error sending VieWUpdate [$viewUpdate]: [${err.message}]", err)
            }
        }
    }

    fun addSession(session: Session, websocketSession: WebSocketServerSession) {
        logger.debug("Adding WebSocketSession to HttpSession [$session]")
        val newSessionSet = sessions[session] ?: mutableSetOf()
        newSessionSet.add(websocketSession)
        sessions[session] = newSessionSet
    }

    fun removeSession(session: Session, websocketSession: WebSocketServerSession) {
        logger.debug("Removing WebSocketSession from HttpSession [$session]")
        val newSessionSet = sessions[session] ?: mutableSetOf()
        newSessionSet.remove(websocketSession)

        if (newSessionSet.count() == 0) {
            sessions.remove(session)
        } else {
            sessions[session] = newSessionSet
        }
    }
}
