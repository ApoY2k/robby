package apoy2k.robby.engine

import apoy2k.robby.model.*
import apoy2k.robby.templates.GameTpl
import io.ktor.server.html.*
import io.ktor.websocket.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.html.body
import kotlinx.html.html
import kotlinx.html.stream.appendHTML
import org.ktorm.database.Database
import org.slf4j.LoggerFactory
import java.time.Clock

data class ViewUpdate(val gameId: Int)

class ViewUpdateRouter(
    private val clock: Clock,
    private val database: Database,
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Keeps a map of existing gameId -> http session -> websocket sessions relations
     * which can be used to send the viewupdates, rendered in the context of the http session, per game
     */
    private val sessions: MutableMap<Int, MutableMap<WebSocketSession, Session?>> = mutableMapOf()

    /**
     * Start listening for view updates to send out tp the registered websocket sessions
     */
    suspend fun connect(updates: SharedFlow<ViewUpdate>) = coroutineScope {
        updates.onEach { update ->
            try {
                val gameSessions = sessions[update.gameId] ?: emptyMap()
                if (gameSessions.isEmpty()) {
                    return@onEach
                }

                val game = database.game(update.gameId) ?: return@onEach
                val board = database.fieldsFor(game.id).toBoard()
                val robots = database.robotsFor(game.id)

                gameSessions.forEach { (wsSession, httpSession) ->
                    val user = database.user(httpSession)
                    val robot = robots.find { user != null && it.userId == user.id }
                    val cards = when (robot != null) {
                        true -> database
                            .cardsForGame(robot.id)
                            .sortedByDescending { it.priority }
                            .map { it }

                        else -> listOf()
                    }

                    val gameView = GameTpl(clock.instant(), game, robots, board, user, robot, cards)
                    val html = StringBuilder()
                        .appendHTML(false)
                        .html { body { insert(gameView) {} } }
                        .toString()

                    val frame = Frame.Text(html)
                    logger.debug("Sending ViewUpdate of Game(${update.gameId}) to $httpSession over $wsSession")
                    wsSession.send(frame)
                }
            } catch (err: Throwable) {
                logger.error("Error sending ViewUpdate: ${err.message}", err)
            }
        }.launchIn(this)
    }

    fun addSession(gameId: Int, wsSession: WebSocketSession, httpSession: Session?) {
        logger.debug("Adding $wsSession of $httpSession to Game($gameId) listeners")
        val gameSessions = sessions[gameId] ?: mutableMapOf()
        gameSessions[wsSession] = httpSession
        sessions[gameId] = gameSessions
    }

    fun removeSession(gameId: Int, wsSession: WebSocketSession) {
        logger.debug("Removing $wsSession from Game($gameId) listeners")
        val gameSessions = sessions[gameId] ?: mutableMapOf()
        gameSessions.remove(wsSession)
    }
}
