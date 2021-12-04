package apoy2k.robby.templates

import apoy2k.robby.data.Storage
import apoy2k.robby.routes.Location
import io.ktor.html.*
import kotlinx.html.*

class Lobby(private val storage: Storage) : Template<FlowContent> {
    override fun FlowContent.apply() {
        div("row") {
            div("col") {
                ul {
                    storage.listGames().forEach {
                        li {
                            a(Location.GAME_VIEW.build(mapOf("id" to it.id))) {
                                +it.id
                            }
                        }
                    }
                }
            }
        }
        div("row") {
            div("col") {
                form(Location.GAME_ROOT.path, method = FormMethod.post) {
                    button(classes = "btn btn-primary") {
                        +"Start new game"
                    }
                }
            }
        }
    }
}
