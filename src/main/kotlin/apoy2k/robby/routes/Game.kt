package apoy2k.robby.routes

import apoy2k.robby.engine.BoardType
import apoy2k.robby.engine.GameEngine
import apoy2k.robby.engine.ViewUpdateRouter
import apoy2k.robby.engine.toBoard
import apoy2k.robby.model.Action
import apoy2k.robby.model.Session
import apoy2k.robby.model.cardsForRobot
import apoy2k.robby.model.fieldsFor
import apoy2k.robby.model.game
import apoy2k.robby.model.robotsFor
import apoy2k.robby.templates.GameTpl
import apoy2k.robby.templates.LayoutTpl
import io.ktor.http.HttpStatusCode
import io.ktor.server.html.insert
import io.ktor.server.html.respondHtmlTemplate
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.response.respondFile
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readBytes
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.MutableSharedFlow
import org.ktorm.database.Database
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
        val boardType = BoardType.valueOf(params["board"].orEmpty())
        gameEngine.createNewGame(boardType)
        call.respondRedirect(Location.ROOT.path)
    }

    route(Location.GAME_VIEW.path) {
        get {
            val gameId = call.parameters["id"]?.toInt()
            if (gameId == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

            val game = database.game(gameId)
            if (game == null) {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }

            val user = getUser(database)
            val board = database.fieldsFor(gameId).toBoard()
            val robots = database.robotsFor(gameId)

            val robot = when (user?.id != null) {
                true -> robots.find { it.userId == user.id }
                else -> null
            }

            val cards = when (robot != null) {
                true -> database
                    .cardsForRobot(robot.id)
                    .sortedByDescending { it.priority }
                    .map { it }

                else -> listOf()
            }

            val gameView = GameTpl(clock.instant(), game, robots, board, user, robot, cards)
            call.respondHtmlTemplate(LayoutTpl(user)) {
                content { insert(gameView) {} }
            }
        }
    }

    webSocket(Location.GAME_WEBSOCKET.path) {
        val session = call.sessions.get<Session>()
        val gameId = call.parameters["id"]?.toInt()
            ?: throw Exception("No game id found")
        val game = database.game(gameId)
            ?: throw Exception("No game with id $gameId found, aborting websocket session")

        viewUpdateRouter.addSession(gameId, this, session)

        // This will block the thread while listening for incoming messages
        incoming.consumeEach { frame ->
            when (frame) {
                is Frame.Text -> {
                    try {
                        val data = frame.readText()
                        logger.debug("Received [{}] from {} on Game({})", data, session, gameId)
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
        viewUpdateRouter.removeSession(gameId, this)
    }

    get(Location.GAME_IMAGE.path) {
        // TODO: generate image preview of game state
        val dummy = javaClass.getResource("/assets/dummy-board-preview.png")?.toURI()!!
        call.respondFile(File(dummy))
    }
}
