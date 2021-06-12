package apoy2k.robby.engine

import apoy2k.robby.data.Storage
import apoy2k.robby.model.Session
import apoy2k.robby.templates.renderGame
import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.html.HtmlBlockTag
import kotlinx.html.body
import kotlinx.html.html
import kotlinx.html.stream.appendHTML
import org.slf4j.LoggerFactory
import java.lang.StringBuilder

/**
 * Manages mapping between HTTP and WebSocket sessions
 */
class WebSocketHandler(private val storage: Storage, private val viewUpdates: ReceiveChannel<Unit>) {
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
        viewUpdates.consumeEach {
            try {
                sessions
                    .forEach { (session, socket) ->
                        // Render the view for the target session, so each session receives their individual
                        // view of the game state
                        val gameView = StringBuilder().appendHTML(false).html {
                            body {
                                renderGame(storage, session)
                            }
                        }.toString()

                        logger.debug("Sending GameView to session [$session] (${socket.count()} sockets)")
                        socket.forEach { it.send(Frame.Text(gameView)) }
                    }
            } catch (err: Throwable) {
                logger.error("Error sending GameView: [${err.message}]", err)
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
