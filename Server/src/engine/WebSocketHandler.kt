package apoy2k.robby.engine

import apoy2k.robby.model.Session
import apoy2k.robby.model.ViewUpdate
import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors

/**
 * Manages mapping between HTTP and WebSocket sessions
 */
class WebSocketHandler {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val executorService = Executors.newFixedThreadPool(1)

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
    fun connect(actions: Channel<ViewUpdate>) {
        executorService.submit {
            runBlocking {
                actions.consumeEach { viewUpdate ->
                    logger.debug("Received [$viewUpdate]")

                    try {
                        sessions
                            .filter { (k, _) ->
                                viewUpdate.recipients.isEmpty() || viewUpdate.recipients.contains(k)
                            }
                            .forEach { (k, v) ->
                                logger.debug("Sending [$viewUpdate] to session [$k] (${v.count()} sockets)")
                                v.forEach { it.send(Frame.Text(viewUpdate.toString())) }
                            }
                    } catch (err: Throwable) {
                        logger.error("Error sending VieWUpdates: [${err.message}]", err)
                    }
                }
            }
        }
    }
}
