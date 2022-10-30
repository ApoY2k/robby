package apoy2k.robby.templates

import apoy2k.robby.routes.Location
import io.ktor.server.html.*
import kotlinx.html.*

class LoginTpl : Template<FlowContent> {
    override fun FlowContent.apply() {
        val action = Location.AUTH.build()
        form(action, method = FormMethod.post) {
            div("row") {
                div("col") {
                    div("input-group") {
                        span("input-group-text") { +"Username" }
                        input(InputType.text, name = "username", classes = "form-control")
                    }
                }
                div("col") {
                    button(classes = "btn btn-primary") { +"Join" }
                }
            }
        }
    }
}
