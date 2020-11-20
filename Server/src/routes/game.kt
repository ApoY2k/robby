package apoy2k.robby.routes

import apoy2k.robby.data.Sockets
import apoy2k.robby.data.Storage
import apoy2k.robby.data.newGame
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.game(storage: Storage, sockets: Sockets) {
    get("/reset") {
        storage.game = newGame()

        sockets.send("refresh:board")

        call.respondRedirect("/")
    }

    get("/switchfield/{id}") {
        val id = call.parameters["id"]
        if (id.isNullOrEmpty()) {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }

        storage.game.board.flip(id)

        sockets.send("refresh:board")

        call.respond(HttpStatusCode.OK)
    }
}
