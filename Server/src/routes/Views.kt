package apoy2k.robby.routes

import apoy2k.robby.data.Storage
import apoy2k.robby.model.Session
import apoy2k.robby.model.View
import apoy2k.robby.templates.*
import io.ktor.application.*
import io.ktor.html.*
import io.ktor.routing.*
import io.ktor.sessions.*
import kotlinx.html.body

fun Route.views(storage: Storage) {
    get(View.BOARD.toString()) {
        call.respondHtml {
            body {
                renderBoard(storage.game, call.sessions.get<Session>())
            }
        }
    }

    get(View.PLAYERS.toString()) {
        call.respondHtml {
            body {
                renderPlayers(storage.game, call.sessions.get<Session>())
            }
        }
    }

    get(View.JOIN_FORM.toString()) {
        call.respondHtml {
            body {
                renderJoinForm(storage.game, call.sessions.get<Session>())
            }
        }
    }

    get(View.PROFILE.toString()) {
        call.respondHtml {
            body {
                renderProfile(storage.game, call.sessions.get<Session>())
            }
        }
    }
}
