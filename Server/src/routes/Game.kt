package apoy2k.robby.routes

import apoy2k.robby.engine.Engine
import apoy2k.robby.engine.ResetBoardCommand
import apoy2k.robby.engine.SwitchFieldCommand
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.game(engine: Engine) {
    get("/reset") {
        engine.perform(listOf(ResetBoardCommand()))
        call.respond(HttpStatusCode.OK)
    }

    get("/switchfield/{id}") {
        val id = call.parameters["id"]
        if (id.isNullOrEmpty()) {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }

        engine.perform(listOf(SwitchFieldCommand(id)))

        call.respond(HttpStatusCode.OK)
    }
}
