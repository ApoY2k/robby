package apoy2k.robby.engine

import apoy2k.robby.model.Game
import apoy2k.robby.model.Session
import apoy2k.robby.model.ViewUpdate
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
import org.slf4j.LoggerFactory

class ViewUpdateRouter {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Keeps a map of existing game -> http sessions -> websocket sessions relations
     * which can be used to send the viewupdates, rendered in the context of the http session, per game
     */
    private val sessions: MutableMap<Game, MutableMap<Session, MutableCollection<WebSocketSession>>> = mutableMapOf()

    /**
     * Start listening for view updates to send out tp the registered websocket sessions
     */
    suspend fun connect(updates: SharedFlow<ViewUpdate>) = coroutineScope {
        updates.onEach { update ->
            try {
                val gameSessions = sessions[update.game] ?: emptyMap()
                gameSessions
                    .forEach { (httpSession, wsSessions) ->
                        wsSessions.forEach { wsSession ->
                            val gameView = StringBuilder().appendHTML(false).html {
                                body {
                                    insert(GameTpl(update.game, httpSession)) {}
                                }
                            }.toString()

                            logger.debug("Sending ViewUpdate to $httpSession for ${update.game}")
                            wsSession.send(Frame.Text(gameView))
                        }
                    }
            } catch (err: Throwable) {
                logger.error("Error sending ViewUpdate: ${err.message}", err)
            }
        }.launchIn(this)
    }

    fun addSession(game: Game, httpSession: Session, wsSession: WebSocketSession) {
        logger.debug("Adding new WebSocketSession of $httpSession to $game listeners")
        val gameSessions = sessions[game] ?: mutableMapOf()
        val wsSessions = gameSessions[httpSession] ?: mutableSetOf()
        wsSessions.add(wsSession)
        gameSessions[httpSession] = wsSessions
        sessions[game] = gameSessions
    }

    fun removeSession(game: Game, httpSession: Session, wsSession: WebSocketSession) {
        logger.debug("Removing WebSocketSession of $httpSession from $game listeners")
        val gameSessions = sessions[game] ?: mutableMapOf()
        val wsSessions = gameSessions[httpSession] ?: mutableSetOf()
        wsSessions.remove(wsSession)
        if (wsSessions.isEmpty()) {
            gameSessions.remove(httpSession)
        }
        if (gameSessions.isEmpty()) {
            sessions.remove(game)
        }
    }
}
