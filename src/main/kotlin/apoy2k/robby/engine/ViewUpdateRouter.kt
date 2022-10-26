package apoy2k.robby.engine

import apoy2k.robby.model.Session
import apoy2k.robby.model.games
import apoy2k.robby.model.unwrapToBoard
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
import org.ktorm.dsl.eq
import org.ktorm.entity.find
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
    private val sessions: MutableMap<Int, MutableMap<Session, MutableCollection<WebSocketSession>>> = mutableMapOf()

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

                val game = database.games.find { it.id eq update.gameId }
                    ?: return@onEach

                gameSessions
                    .forEach { (httpSession, wsSessions) ->
                        val gameView = StringBuilder()
                            .appendHTML(false)
                            .html {
                                body {
                                    insert(
                                        GameTpl(
                                            clock.instant(),
                                            game,
                                            game.robots(database),
                                            game.fields(database).unwrapToBoard(),
                                            httpSession,
                                        )
                                    ) {}
                                }
                            }.toString()
                        val frame = Frame.Text(gameView)

                        wsSessions.forEach { wsSession ->
                            logger.debug("Sending ViewUpdate of Game(${update.gameId}) to $httpSession")
                            wsSession.send(frame)
                        }
                    }
            } catch (err: Throwable) {
                logger.error("Error sending ViewUpdate: ${err.message}", err)
            }
        }.launchIn(this)
    }

    fun addSession(gameId: Int, httpSession: Session, wsSession: WebSocketSession) {
        logger.debug("Adding new WebSocketSession of $httpSession to Game($gameId) listeners")
        val gameSessions = sessions[gameId] ?: mutableMapOf()
        val wsSessions = gameSessions[httpSession] ?: mutableSetOf()
        wsSessions.add(wsSession)
        gameSessions[httpSession] = wsSessions
        sessions[gameId] = gameSessions
    }

    fun removeSession(gameId: Int, httpSession: Session, wsSession: WebSocketSession) {
        logger.debug("Removing WebSocketSession of $httpSession from Game($gameId) listeners")
        val gameSessions = sessions[gameId] ?: mutableMapOf()
        val wsSessions = gameSessions[httpSession] ?: mutableSetOf()
        wsSessions.remove(wsSession)
        if (wsSessions.isEmpty()) {
            gameSessions.remove(httpSession)
        }
        if (gameSessions.isEmpty()) {
            sessions.remove(gameId)
        }
    }
}
