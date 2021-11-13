package apoy2k.robby.engine

import apoy2k.robby.model.Game
import apoy2k.robby.model.Session
import apoy2k.robby.model.ViewUpdate
import apoy2k.robby.templates.GameTpl
import io.ktor.html.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.html.body
import kotlinx.html.html
import kotlinx.html.stream.appendHTML
import org.slf4j.LoggerFactory
import java.lang.StringBuilder

@ExperimentalCoroutinesApi
class ViewUpdateRouter {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Keep a map of existing game -> http session -> websocket session relations
     * which can be used to send the viewupdates, renderd in the context of the http session, per game
     */
    private val sessions: MutableMap<Game, MutableMap<Session, MutableCollection<WebSocketSession>>> = mutableMapOf()

    /**
     * Start listening for view updates to send out tp the registered websocket sessions
     */
    suspend fun connect(updates: ReceiveChannel<ViewUpdate>) {
        updates.consumeEach { update ->
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

                            logger.debug("Sending ViewUpdate to $httpSession for ${update.game} on $wsSession")
                            wsSession.send(Frame.Text(gameView))
                        }
                    }
            } catch (err: Throwable) {
                logger.error("Error sending GameView: [${err.message}]", err)
            }
        }
    }

    fun addSession(game: Game, httpSession: Session, wsSession: WebSocketSession) {
        logger.debug("Adding $wsSession of $httpSession to $game listeners")
        val gameSessions = sessions[game] ?: mutableMapOf()
        val wsSessions = gameSessions[httpSession] ?: mutableSetOf()
        wsSessions.add(wsSession)
        gameSessions[httpSession] = wsSessions
        sessions[game] = gameSessions
    }

    fun removeSession(game: Game, httpSession: Session, wsSession: WebSocketSession) {
        logger.debug("Removing $wsSession of $httpSession from $game listeners")
        val gameSessions = sessions[game] ?: mutableMapOf()
        val wsSessions = gameSessions[httpSession] ?: mutableSetOf()
        wsSessions.remove(wsSession)
        if (wsSessions.isEmpty()) {
            gameSessions.remove(httpSession)
        }
        if (gameSessions.isEmpty()) {
            sessions.remove(game);
        }
    }
}
