package apoy2k.robby.routes

import apoy2k.robby.CommandField
import apoy2k.robby.engine.Engine
import apoy2k.robby.model.JoinGameCommand
import apoy2k.robby.model.LeaveGameCommand
import apoy2k.robby.model.ResetBoardCommand
import apoy2k.robby.model.Session
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import org.slf4j.LoggerFactory

fun Route.game(engine: Engine) {
    get("/reset") {
        val session = call.sessions.get<Session>()
        engine.perform(listOf(ResetBoardCommand()), session)
        call.respond(HttpStatusCode.OK)
    }

    post("/join") {
        val session = call.sessions.get<Session>()
        val form = call.receiveParameters()
        engine.perform(listOf(JoinGameCommand(form[CommandField.PLAYER_NAME.name])), session)
        call.respondRedirect("/")
    }

    post("/leave") {
        val session = call.sessions.get<Session>()
        engine.perform(listOf(LeaveGameCommand()), session)
        call.sessions.clear<Session>()
        call.respondRedirect("/")


    }
}
