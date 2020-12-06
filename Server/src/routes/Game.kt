package apoy2k.robby.routes

import apoy2k.robby.model.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import kotlinx.coroutines.channels.SendChannel

fun Route.game(actions: SendChannel<Action>) {
    get("/reset") {
        val session = call.sessions.get<Session>()
        actions.send(ResetBoardAction().also { it.session = session })
        call.respond(HttpStatusCode.OK)
    }

    post("/join") {
        val session = call.sessions.get<Session>()
        val form = call.receiveParameters()
        actions.send(JoinGameAction(form[ActionField.PLAYER_NAME.name]).also { it.session = session })
        call.respondRedirect("/")
    }

    post("/leave") {
        val session = call.sessions.get<Session>()
        actions.send(LeaveGameAction().also { it.session = session })
        call.sessions.clear<Session>()
        call.respondRedirect("/")
    }
}
