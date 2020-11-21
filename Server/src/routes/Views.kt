package apoy2k.robby.routes

import apoy2k.robby.VIEW_BOARD
import apoy2k.robby.data.Storage
import apoy2k.robby.templates.gameRender
import io.ktor.application.*
import io.ktor.html.*
import io.ktor.routing.*
import kotlinx.html.body

fun Route.views(storage: Storage) {
    get(VIEW_BOARD) {
        call.respondHtml {
            body {
                gameRender(storage.game)
            }
        }
    }
}
