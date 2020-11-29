package apoy2k.robby.routes

import apoy2k.robby.VIEW_BOARD
import apoy2k.robby.VIEW_GAME
import apoy2k.robby.VIEW_JOIN_FORM
import apoy2k.robby.VIEW_PLAYERS
import apoy2k.robby.data.Storage
import apoy2k.robby.model.Session
import apoy2k.robby.templates.renderBoard
import apoy2k.robby.templates.renderGame
import apoy2k.robby.templates.renderJoinForm
import apoy2k.robby.templates.renderPlayers
import io.ktor.application.*
import io.ktor.html.*
import io.ktor.routing.*
import io.ktor.sessions.*
import kotlinx.html.body

fun Route.views(storage: Storage) {
    get(VIEW_GAME) {
        call.respondHtml {
            body {
                renderGame(storage.game, call.sessions.get<Session>())
            }
        }
    }

    get(VIEW_BOARD) {
        call.respondHtml {
            body {
                renderBoard(storage.game, call.sessions.get<Session>())
            }
        }
    }

    get(VIEW_PLAYERS) {
        call.respondHtml {
            body {
                renderPlayers(storage.game, call.sessions.get<Session>())
            }
        }
    }

    get(VIEW_JOIN_FORM) {
        call.respondHtml {
            body {
                renderJoinForm(storage.game, call.sessions.get<Session>())
            }
        }
    }
}
