package apoy2k.robby.templates

import apoy2k.robby.data.Storage
import apoy2k.robby.model.Session
import apoy2k.robby.routes.Location
import io.ktor.html.*
import kotlinx.html.*

class HomeTpl(val storage: Storage, val session: Session?) : Template<FlowContent> {
    override fun FlowContent.apply() {
        if (session?.name?.isNotBlank() == true) {
            div("row") {
                div("col") {
                    +"Welcome back, ${session.name}"
                }
            }
            insert(Lobby(storage)) {}
        } else {
            div("row") {
                div("col") {
                    p {
                        +"Welcome to robby"
                    }
                    p {
                        +"Choose a username to use while playing and then join a game or create a new one"
                    }
                }
            }
            val action = Location.SET_USERNAME.build()
            form(action, method = FormMethod.post) {
                div("row") {
                    div("col") {
                        div("input-group") {
                            span("input-group-text") {
                                +"Username"
                            }
                            input(InputType.text, name = "username", classes = "form-control")
                        }
                    }
                    div("col") {
                        button(classes = "btn btn-primary") {
                            +"Save"
                        }
                    }
                }
            }
        }
    }
}
