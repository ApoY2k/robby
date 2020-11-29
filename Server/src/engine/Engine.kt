package apoy2k.robby.engine

import apoy2k.robby.data.Storage
import apoy2k.robby.model.Command
import apoy2k.robby.model.Session
import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import org.slf4j.LoggerFactory

/**
 * Connecting engine between the game all socket sessions
 */
class Engine(private val storage: Storage) {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    val sessions = HashMap<Session, MutableSet<WebSocketServerSession>>()

    /**
     * Perform a set of command on to mutate the game state, in order, executed in the context
     * of a given session
     */
    suspend fun perform(commands: List<Command>, session: Session?) {
        if (session == null) {
            logger.error("No session associated with command list")
            return
        }

        try {
            val resultCommands = commands.flatMap { storage.game.perform(it, session) }.toSet()
            send(resultCommands)
        } catch (err: Throwable) {
            logger.error("Engine error: ${err.message}", err)
        }
    }

    /**
     * Send a set of commands to all sessions
     */
    private suspend fun send(commands: Set<Command>) {
        logger.debug(commands.toString())
        commands.forEach { command ->
            logger.debug(command.toString())
            sessions
                .filter { s -> command.recipients.isEmpty() || command.recipients.any { r -> r.session == s.key } }
                .forEach { sessionEntry ->
                    val sockets = sessionEntry.value
                    logger.debug("Sending [$command] to session [${sessionEntry.key}] (${sockets.count()} sockets)")
                    sockets.forEach { it.send(Frame.Text(command.toString())) }
                }
        }
    }
}
