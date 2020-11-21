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
}
