package apoy2k.robby

import apoy2k.robby.data.MemoryStorage
import apoy2k.robby.engine.GameEngine
import apoy2k.robby.engine.ViewUpdateRouter
import apoy2k.robby.model.Action
import apoy2k.robby.model.Session
import apoy2k.robby.model.ViewUpdate
import apoy2k.robby.routes.base
import apoy2k.robby.routes.game
import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.apache.commons.lang3.RandomStringUtils
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import kotlin.collections.set

fun main(args: Array<String>): Unit = EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    val logger = LoggerFactory.getLogger(this.javaClass)

    install(CallLogging) {
        level = Level.DEBUG
    }

    install(ContentNegotiation) {
        gson {
        }
    }

    install(WebSockets) {
    }

    install(Resources) {
    }

    install(Sessions) {
        cookie<Session>("SESSION", SessionStorageMemory()) {
            cookie.extensions["SameSite"] = "lax"
        }
    }

    intercept(ApplicationCallPipeline.Call) {
        val session = call.sessions.get<Session>()
        if (session == null) {
            call.sessions.set(Session(RandomStringUtils.randomAlphanumeric(5)))
        }
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            logger.error("Unhandled error ${cause.message}", cause)
            call.respond(HttpStatusCode.InternalServerError, cause.message.toString())
        }
    }

    val storage = MemoryStorage()
    val actionChannel = MutableSharedFlow<Action>()
    val viewUpdateChannel = MutableSharedFlow<ViewUpdate>()

    val engine = GameEngine(viewUpdateChannel)
    launch {
        engine.connect(actionChannel)
    }

    val viewUpdateRouter = ViewUpdateRouter()
    launch {
        viewUpdateRouter.connect(viewUpdateChannel)
    }

    routing {
        base(storage)
        game(actionChannel, viewUpdateRouter, storage)
    }
}
