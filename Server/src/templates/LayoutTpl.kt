package apoy2k.robby.templates

import io.ktor.html.*
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
                href = "https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css"
            ) {
                attributes["integrity"] = "sha384-1BmE4kWBq78iYhFldvKuhfTAU6auU8tT94WrHftjDbrCEXSU1oBoqyl2QvZ6jIW3"
                attributes["crossorigin"] = "anonymous"
            }
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
            script(
                "text/javascript",
                src = "https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"
            ) {
                attributes["integrity"] = "sha384-ka7Sk0Gln4gmtz2MlQnikT1wXgYsOg+OMhuP+IlRH9sENBO0LRn5q+8nbTov4+1p"
                attributes["crossorigin"] = "anonymous"
            }
            script("text/javascript", src = "/static/main.js") { }
        }
    }
}
