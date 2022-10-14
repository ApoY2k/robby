package apoy2k.robby.templates

import io.ktor.server.html.*
import kotlinx.html.*

class LayoutTpl : Template<HTML> {
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
            div("container") {
                div("row") {
                    div("col") {
                        h1 { +"robby" }
                    }
                }
            }
            div("container") {
                insert(content)
            }
            div("container") {
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
