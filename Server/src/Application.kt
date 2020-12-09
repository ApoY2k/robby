package apoy2k.robby

import apoy2k.robby.data.MemoryStorage
import apoy2k.robby.engine.GameEngine
import apoy2k.robby.engine.WebSocketHandler
import apoy2k.robby.model.Action
import apoy2k.robby.model.Session
import apoy2k.robby.model.ViewUpdate
import apoy2k.robby.routes.base
import apoy2k.robby.routes.game
import apoy2k.robby.routes.socket
import apoy2k.robby.routes.views
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.netty.*
import io.ktor.sessions.*
import io.ktor.websocket.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.slf4j.event.Level
import java.util.concurrent.ThreadLocalRandom

fun main(args: Array<String>): Unit = EngineMain.main(args)

@ExperimentalCoroutinesApi
@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    install(CallLogging) {
        level = Level.DEBUG
    }

    install(ContentNegotiation) {
        gson {
        }
    }

    install(WebSockets) {
    }

    install(Sessions) {
        cookie<Session>("SESSION", SessionStorageMemory())
    }

    intercept(ApplicationCallPipeline.Call) {
        val session = call.sessions.get<Session>()
        if (session == null) {
            call.sessions.set(Session(ThreadLocalRandom.current().nextLong().toString()))
        }
    }

    install(StatusPages) {
        exception<Throwable> {
            call.respond(HttpStatusCode.InternalServerError, it.message.toString())
            throw it
        }

        status(HttpStatusCode.NotFound) {
            call.respond(HttpStatusCode.NotFound, "Route to [${call.request.uri}] not found")
        }
    }

    val storage = MemoryStorage()
    val actionChannel = Channel<Action>()
    val viewUpdateChannel = Channel<ViewUpdate>()

    val engine = GameEngine(storage, actionChannel, viewUpdateChannel)
    launch {
        engine.connect()
    }

    val webSocketHandler = WebSocketHandler(viewUpdateChannel)
    launch {
        webSocketHandler.connect()
    }

    routing {
        base(storage)
        game(actionChannel)
        socket(webSocketHandler, actionChannel)
        views(storage)
    }
}
