package apoy2k.robby

import apoy2k.robby.engine.GameEngine
import apoy2k.robby.engine.RobotEngine
import apoy2k.robby.engine.ViewUpdate
import apoy2k.robby.engine.ViewUpdateRouter
import apoy2k.robby.model.Action
import apoy2k.robby.model.DbSessionStorage
import apoy2k.robby.model.Session
import apoy2k.robby.routes.auth
import apoy2k.robby.routes.base
import apoy2k.robby.routes.game
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticFiles
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.resources.Resources
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import io.ktor.server.sessions.Sessions
import io.ktor.server.sessions.cookie
import io.ktor.server.websocket.WebSockets
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.ktorm.database.Database
import org.ktorm.logging.Slf4jLoggerAdapter
import org.ktorm.support.sqlite.SQLiteDialect
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import java.io.File
import java.time.Clock

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        val clock = Clock.systemDefaultZone()
        val database = Database.connect(
            url = "jdbc:sqlite:robby.db",
            dialect = SQLiteDialect(),
            logger = Slf4jLoggerAdapter("org.ktorm.database"),
        )

        setup(clock, database)
    }.start(wait = true)
}

fun Application.setup(
    clock: Clock,
    database: Database,
) {
    val logger = LoggerFactory.getLogger(this.javaClass)

    install(WebSockets)

    install(Resources)

    install(CallLogging) {
        level = Level.TRACE
    }

    install(ContentNegotiation) {
        json()
    }

    install(Sessions) {
        cookie<Session>("SESSION", DbSessionStorage(database)) {
            cookie.extensions["SameSite"] = "lax"
        }
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            logger.error("Unhandled error ${cause.message}", cause)
            call.respond(HttpStatusCode.InternalServerError, cause.message.toString())
        }
    }

    val actionChannel = MutableSharedFlow<Action>()
    val viewUpdateChannel = MutableSharedFlow<ViewUpdate>()
    val robotEngine = RobotEngine(database)
    val gameEngine = GameEngine(clock, database, robotEngine, viewUpdateChannel)
    val viewUpdateRouter = ViewUpdateRouter(clock, database)

    launch {
        gameEngine.connect(actionChannel)
    }

    launch {
        viewUpdateRouter.connect(viewUpdateChannel)
    }

    routing {
        staticFiles("assets", File("assets"))
        base(clock, database)
        auth(database)
        game(clock, database, gameEngine, actionChannel, viewUpdateRouter)
    }

    // TODO initialize database schema if empty
}
