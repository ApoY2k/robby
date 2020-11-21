package apoy2k.robby.routes

import apoy2k.robby.data.Storage
import apoy2k.robby.templates.renderGame
import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.content.*
import io.ktor.routing.*
import kotlinx.html.*

fun Route.base(storage: Storage) {
    get("/") {
        call.respondHtml {
            head {
                title("robby")
                link(
                    rel = "stylesheet",
                    href = "https://cdn.jsdelivr.net/npm/bootstrap@4.5.3/dist/css/bootstrap.min.css"
                )
                link(rel = "stylesheet", href = "/static/main.css")
            }
            body {
                div(classes = "container") {
                    div(classes = "row") {
                        div(classes = "col") {
                            p {
                                a(href = "/reset") {
                                    +"Reset"
                                }
                            }
                        }
                    }
                }
                div(classes = "container") {
                    renderGame(storage.game)
                }
                script(src = "/static/main.js") { }
            }
        }
    }

    static("/static") {
        resources("static")
    }
}