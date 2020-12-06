package apoy2k.robby

import apoy2k.robby.data.MemoryStorage
import apoy2k.robby.engine.GameEngine
import apoy2k.robby.engine.WebSocketHandler
import apoy2k.robby.model.Action
import apoy2k.robby.model.Session
import apoy2k.robby.model.View
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import java.util.concurrent.ThreadLocalRandom

fun main(args: Array<String>): Unit = EngineMain.main(args)

@ExperimentalCoroutinesApi
@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    val logger = LoggerFactory.getLogger("root")

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
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

    val engine = GameEngine(storage)
    launch {
        engine.connect(actionChannel, viewUpdateChannel)
    }

    val webSocketHandler = WebSocketHandler()
    launch {
        webSocketHandler.connect(viewUpdateChannel)
    }

    launch {
        while (true) {
            try {
                // Catch the ExecuteMovement action as it's an entirely internal action
                // and can be executed without any associated session. If a movement was executed,
                // trigger a ViewUpdate for the board which is broadcast to all players
                if (engine.movementsToExecute.isNotEmpty()) {
                    engine.executeNextMovement()
                    viewUpdateChannel.send(ViewUpdate(View.BOARD))

                    // Before finishing, delay for some amount of time so the clients can render the new state
                    // and players can see the new state nicely
                    delay(1000)
                    continue
                }
            } catch (err: Throwable) {
                logger.error("Error in movement execution: [${err.message}]", err)
            }

            delay(500)
        }
    }

    routing {
        base(storage)
        game(actionChannel)
        socket(webSocketHandler, actionChannel)
        views(storage)
    }
}
