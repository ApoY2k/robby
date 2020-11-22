package apoy2k.robby

import apoy2k.robby.data.MemoryStorage
import apoy2k.robby.engine.Engine
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
import io.ktor.websocket.*
import org.slf4j.event.Level

fun main(args: Array<String>): Unit = EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
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

    install(StatusPages) {
        exception<Throwable> {
            call.respond(HttpStatusCode.InternalServerError, it.message.toString())
            throw it
        }

        status(HttpStatusCode.NotFound) {
            call.respond(HttpStatusCode.NotFound, "Route to ${call.request.uri} not found")
        }
    }

    val storage = MemoryStorage()
    val engine = Engine(storage)

    routing {
        base(storage)
        game(engine)
        socket(engine)
        views(storage)
    }
}
