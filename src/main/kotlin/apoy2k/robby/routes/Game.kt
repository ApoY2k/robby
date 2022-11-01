package apoy2k.robby.routes

import apoy2k.robby.engine.BoardEngine
import apoy2k.robby.engine.BoardType
import apoy2k.robby.engine.GameEngine
import apoy2k.robby.engine.ViewUpdateRouter
import apoy2k.robby.model.*
import apoy2k.robby.templates.GameTpl
import apoy2k.robby.templates.LayoutTpl
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.MutableSharedFlow
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.map
import org.ktorm.entity.sortedByDescending
import org.slf4j.LoggerFactory
import java.io.File
import java.time.Clock

fun Route.game(
    clock: Clock,
    database: Database,
    gameEngine: GameEngine,
    actions: MutableSharedFlow<Action>,
    viewUpdateRouter: ViewUpdateRouter,
) {
    val logger = LoggerFactory.getLogger("${this.javaClass.name}.game")

    post(Location.GAME_ROOT.path) {
        val params = call.receiveParameters()
        val type = BoardType.valueOf(params["board"].orEmpty())
        gameEngine.createNewGame(type)
        call.respondRedirect(Location.ROOT.path)
    }

    route(Location.GAME_VIEW.path) {
        get {
            val gameId = call.parameters["id"]?.toInt()
            if (gameId == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

            val game = database.games.find { it.id eq gameId }
            if (game == null) {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }

            val fields = database.fields.filter { it.gameId eq gameId }.map { it }
            val board = BoardEngine.fieldListToMatrix(fields)
            val robots = database.robots.filter { it.gameId eq gameId }.map { it }

            val session = call.sessions.get<Session>()
            val user = database.users.find { it.id eq (session?.userId ?: -1) }
            val currentRobot = when (session?.userId != null) {
                true -> robots.find { it.userId == session?.userId }
                else -> null
            }
            val cards = when (currentRobot != null) {
                true -> database.cards
                    .filter { it.robotId eq currentRobot.id }
                    .sortedByDescending { it.priority }
                    .map { it }

                else -> listOf()
            }

            call.respondHtmlTemplate(LayoutTpl(user)) {
                content {
                    insert(
                        GameTpl(
                            clock.instant(),
                            game,
                            robots,
                            board,
                            user,
                            currentRobot,
                            cards
                        )
                    ) {}
                }
            }
        }
    }

    webSocket(Location.GAME_WEBSOCKET.path) {
        val session = call.sessions.get<Session>()
            ?: throw Exception("No session for websocket found")
        val gameId = call.parameters["id"]?.toInt()
            ?: throw Exception("No game id found")
        val game = database.games.find { it.id eq gameId }
            ?: throw Exception("No game with id $gameId found, aborting websocket session")

        viewUpdateRouter.addSession(gameId, session, this)

        // This will block the thread while listening for incoming messages
        incoming.consumeEach { frame ->
            when (frame) {
                is Frame.Text -> {
                    try {
                        val data = frame.readText()
                        logger.debug("Received [$data] from $session on Game($gameId)")
                        val action = Action.deserializeFromSocket(data).also {
                            it.game = game
                            it.session = session
                        }
                        actions.emit(action)
                    } catch (err: Throwable) {
                        logger.error(err.message, err)
                    }
                }

                else -> {
                    val data = frame.readBytes().toString()
                    logger.warn("Unknown socket message [$data]")
                }
            }
        }

        // At this point, the websocket connection was aborted (by either client or server)
        viewUpdateRouter.removeSession(gameId, session, this)
    }

    get(Location.GAME_IMAGE.path) {
        // TODO: generate image preview of game state
        val dummy = javaClass.getResource("/assets/dummy-board-preview.png").toURI()
        call.respondFile(File(dummy))
    }
}
