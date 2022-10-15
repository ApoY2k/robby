package apoy2k.robby.templates

import apoy2k.robby.data.Storage
import apoy2k.robby.routes.Location
import io.ktor.server.html.*
import kotlinx.html.*

class Lobby(private val storage: Storage) : Template<FlowContent> {
    override fun FlowContent.apply() {
        div("row") {
            div("col") {
                h2 { +"Available games" }
            }
            div("col-2") {
                renderCreateGameButton()
            }
        }
        div("row") {
            storage.listGames().forEach {
                div("col-3") {
                    a(Location.GAME_VIEW.build(mapOf("id" to it.id)), classes = "card") {
                        img("Game Preview", "/game/${it.id}/image", "card-img-top")
                        div("card-body") {
                            h5("card-title") { +it.id }
                        }
                        ul("list-group list-group-flush") {
                            // TODO: show game run info (started/ended)
                            // TODO: show joined players
                            li("list-group-item") { +"<board type>" }
                            li("list-group-item") { +"<more info>" }
                        }
                    }
                }
            }
        }
    }
}
