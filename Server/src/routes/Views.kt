package apoy2k.robby.routes

import apoy2k.robby.VIEW_BOARD
import apoy2k.robby.VIEW_PLAYERS
import apoy2k.robby.data.Storage
import apoy2k.robby.templates.renderBoard
import apoy2k.robby.templates.renderPlayers
import io.ktor.application.*
import io.ktor.html.*
import io.ktor.routing.*
import kotlinx.html.body

fun Route.views(storage: Storage) {
    get(VIEW_BOARD) {
        call.respondHtml {
            body {
                renderBoard(storage.game.board)
            }
        }
    }

    get(VIEW_PLAYERS) {
        call.respondHtml {
            body {
                renderPlayers(storage.game.players)
            }
        }
    }
}
