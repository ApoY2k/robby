package apoy2k.robby.templates

import apoy2k.robby.model.User
import apoy2k.robby.routes.Location
import io.ktor.server.html.*
import kotlinx.html.*

class LayoutTpl(
    private val user: User?
) : Template<HTML> {
    val content = Placeholder<FlowContent>()
    override fun HTML.apply() {
        attributes["lang"] = "en"

        head {
            title("robby")
            meta("viewport", "width=device-width, initial-scale=1")
            link(
                rel = "stylesheet",
                href = "https://cdn.jsdelivr.net/npm/bootstrap@5.2.2/dist/css/bootstrap.min.css"
            ) {
                attributes["integrity"] = "sha384-Zenh87qX5JnK2Jl0vWa8Ck2rdkQ2Bzep5IDxbcnCeuOxjzrPF/et3URy9Bv1WTRi"
                attributes["crossorigin"] = "anonymous"
            }
            link(rel = "stylesheet", href = "/assets/css/main.css")
        }
        body {
            nav("navbar navbar-expand-sm navbar-dark bg-dark mb-3") {
                div("container") {
                    a(Location.ROOT.path, classes = "navbar-brand") { +"🤖 robby" }
                    div("collapse navbar-collapse justify-content-between") {
                        ul("navbar-nav") {
                            li("nav-item") {
                                a(Location.ROOT.path, classes = "nav-link") { +"Games" }
                            }
                            li("nav-itm") {
                                a(Location.BOARDS_VIEW.path, classes = "nav-link") { +"Boards Preview" }
                            }
                        }
                        ul("navbar-nav") {
                            li("nav-item") {
                                if (user != null) {
                                    span("navbar-text") { +user.name }
                                } else {
                                    a(Location.AUTH.path, classes = "nav-link") { +"Login" }
                                }
                            }
                        }
                    }
                }
            }
            main("container") {
                insert(content)
            }
            footer("container") {
                div("row mt-3") {
                    div("col") {
                        hr {}
                        p("text-center text-muted") {
                            small {
                                +"Icons made by "
                                a("https://smashicons.com/") { +"Smashicons" }
                            }
                        }
                    }
                }
            }
            script(
                "text/javascript",
                src = "https://cdn.jsdelivr.net/npm/bootstrap@5.2.2/dist/js/bootstrap.bundle.min.js"
            ) {
                attributes["integrity"] = "sha384-OERcA2EqjJCMA+/3y+gxIOqMEjwtxJY7qPCqsdltbNJuaOe923+mo//f6V8Qbsw3"
                attributes["crossorigin"] = "anonymous"
            }
            script("text/javascript", src = "/assets/js/main.js") { }
        }
    }
}
