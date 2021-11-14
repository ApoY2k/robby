package apoy2k.robby

import apoy2k.robby.data.MemoryStorage
import apoy2k.robby.engine.GameEngine
import apoy2k.robby.engine.ViewUpdateRouter
import apoy2k.robby.model.Action
import apoy2k.robby.model.Session
import apoy2k.robby.model.ViewUpdate
import apoy2k.robby.routes.base
import apoy2k.robby.routes.game
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.netty.*
import io.ktor.sessions.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import java.util.*

fun main(args: Array<String>): Unit = EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
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

    install(Locations) {
    }

    install(Sessions) {
        cookie<Session>("SESSION", SessionStorageMemory()) {
            cookie.extensions["SameSite"] = "lax"
        }
    }

    intercept(ApplicationCallPipeline.Call) {
        val session = call.sessions.get<Session>()
        if (session == null) {
            call.sessions.set(Session(UUID.randomUUID().toString()))
        }
    }

    install(StatusPages) {
        exception<Throwable> {
            logger.error("Unhandled error ${it.message}", it)
            call.respond(HttpStatusCode.InternalServerError, it.message.toString())
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
