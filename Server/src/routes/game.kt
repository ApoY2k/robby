package apoy2k.robby.routes

import apoy2k.robby.data.Storage
import apoy2k.robby.data.newGame
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.game(storage: Storage) {
    get("/reset") {
        storage.game = newGame()
        call.respondRedirect("/")
    }

    get("/switchfield/{id}") {
        val id = call.parameters["id"]
        if (id.isNullOrEmpty()) {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }

        storage.game.board.flip(id)

        //socket.sendUpdate(component.id)

        call.respond(HttpStatusCode.OK)
    }
}