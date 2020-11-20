package apoy2k.robby

import apoy2k.robby.data.MemoryStorage
import apoy2k.robby.data.Sockets
import apoy2k.robby.routes.base
import apoy2k.robby.routes.game
import apoy2k.robby.routes.socket
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.request.*
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

    val storage = MemoryStorage()
    val sockets = Sockets()

    routing {
        base(storage)
        game(storage, sockets)
        socket(sockets)
    }
}
