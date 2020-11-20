package apoy2k.robby.engine

import apoy2k.robby.data.Storage
import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import org.slf4j.LoggerFactory

/**
 * Root game engine that handles changing the game state and messaging socket sessions to inform about updates
 */
class Engine(private val storage: Storage) {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    val sessions = HashSet<WebSocketServerSession>()

    /**
     * Perform a set of command on to mutate the game state, in order
     */
    suspend fun perform(commands: List<Command>) {
        send(storage.game.perform(commands))
    }

    /**
     * Parse a command and perform it
     */
    suspend fun perform(str: String) {
        try {
            perform(listOf(str.toCommand()))
        } catch (exc: Exception) {
            logger.error("Engine error: ${exc.message}", exc)
        }
    }

    /**
     * Send a set of commands to all sessions
     */
    private suspend fun send(commands: Set<Command>) {
        commands.forEach { command ->
            logger.debug("Sending [$command]")
            sessions.forEach { it.send(Frame.Text(command.toString())) }
        }
    }
}
