package apoy2k.robby.routes

import apoy2k.robby.data.Storage
import apoy2k.robby.model.Session
import apoy2k.robby.templates.renderGame
import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.content.*
import io.ktor.routing.*
import io.ktor.sessions.*
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
                div("container") {
                    div("row") {
                        div("col") {
                            h1 {
                                +"robby"
                            }
                        }
                    }
                    renderGame(storage, call.sessions.get<Session>())
                }
                div("container") {
                    div("row mt-3") {
                        div("col") {
                            hr{}
                            p("text-center text-muted") {
                                small {
                                    +"Icons made by "
                                    a("https://www.flaticon.com/authors/smashicons") {
                                        +"Smashicons"
                                    }
                                    +" from "
                                    a("https://www.flaticon.com/") {
                                        +"www.flaticon.com"
                                    }
                                }
                            }
                        }
                    }
                }
                script("text/javascript", "https://code.jquery.com/jquery-3.4.1.slim.min.js") { }
                script(
                    "text/javascript",
                    src = "https://cdn.jsdelivr.net/npm/bootstrap@4.5.3/dist/js/bootstrap.bundle.min.js"
                ) { }
                script("text/javascript", src = "/static/main.js") { }
            }
        }
    }

    static("/static") {
        resources("static")
    }
}
