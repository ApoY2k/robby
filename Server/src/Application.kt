package apoy2k.robby

import apoy2k.robby.engine.Game
import apoy2k.robby.model.Board
import apoy2k.robby.model.Field
import apoy2k.robby.templates.gameRender
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.netty.*
import io.ktor.html.*
import io.ktor.http.*
import kotlinx.html.*
import org.slf4j.event.Level

fun main(args: Array<String>): Unit = EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(ContentNegotiation) {
        gson {
        }
    }

    fun newGame(): Game {
        return Game(Board(
            arrayOf(
                arrayOf(Field(), Field(), Field(), Field()),
                arrayOf(Field(), Field(), Field(), Field()),
                arrayOf(Field(), Field(), Field(), Field()),
                arrayOf(Field(), Field(), Field(), Field()),
            )
        ))
    }

    var game = newGame()

    routing {
        get("/") {
            call.respondHtml {
                head {
                    title("robby")
                    link(rel = "stylesheet", href = "https://cdn.jsdelivr.net/npm/bootstrap@4.5.3/dist/css/bootstrap.min.css")
                    link(rel = "stylesheet", href="/static/main.css")
                }
                body {
                    div(classes = "container") {
                        div(classes = "row") {
                            div(classes = "col") {
                                h1() {
                                    +"robby"
                                }
                            }
                        }
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
                    gameRender(game)
                    script(src = "/static/main.js") { }
                }
            }
        }

        get("/switchfield/{id}") {
            val id = call.parameters["id"]
            if (id.isNullOrEmpty()) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

            game.board.flip(id)
            call.respondRedirect("/")
        }

        get("/reset") {
            game = newGame()
            call.respondRedirect("/")
        }

        static("/static") {
            resources("static")
        }
    }
}
