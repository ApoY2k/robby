package apoy2k.robby.engine

import apoy2k.robby.data.Storage
import apoy2k.robby.model.Command
import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import org.slf4j.LoggerFactory

/**
 * Connecting engine between the game all socket sessions
 */
class Engine(private val storage: Storage) {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    val sessions = HashSet<WebSocketServerSession>()

    /**
     * Perform a set of command on to mutate the game state, in order
     */
    suspend fun perform(commands: List<Command>) {
        try {
            send(storage.game.perform(commands))
            logger.info(storage.game.players.toString())
        } catch (err: Throwable) {
            logger.error("Engine error: ${err.message}", err)
        }
    }

    /**
     * Parse a command and perform it
     */
    suspend fun perform(str: String) {
        perform(listOf(Command.fromString(str)))
    }

    /**
     * Send a set of commands to all sessions
     */
    private suspend fun send(commands: Set<Command>) {
        commands.forEach { command ->
            logger.debug("Sending [$command] to [${sessions.count()}] sessions")
            sessions.forEach { it.send(Frame.Text(command.toString())) }
        }
    }
}
