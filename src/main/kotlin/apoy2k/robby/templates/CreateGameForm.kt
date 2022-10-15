package apoy2k.robby.templates

import apoy2k.robby.model.BoardType
import apoy2k.robby.routes.Location
import kotlinx.html.*

fun HtmlBlockTag.renderCreateGameButton() {
    button(classes = "btn btn-primary") {
        attributes["data-bs-toggle"] = "modal"
        attributes["data-bs-target"] = "#create-game-modal"
        +"Start new game"
    }
    renderCreateGameModal()
}

fun HtmlBlockTag.renderCreateGameModal() {
    div("modal fade") {
        attributes["id"] = "create-game-modal"

        div("modal-dialog") {
            div("modal-content") {
                div("modal-header") {
                    h5("modal-title") { +"Create new game" }
                    button(classes = "close btn-close") {
                        attributes["data-bs-dismiss"] = "modal"
                    }
                }
                div("modal-body") {
                    renderCreateGameForm()
                }
            }
        }
    }
}

fun HtmlBlockTag.renderCreateGameForm() {
    form(Location.GAME_ROOT.path, method = FormMethod.post) {
        div("mb-3") {
            label("form-label") {
                attributes["for"] = "board"
                +"Board"
            }
            select("form-control") {
                attributes["name"] = "board"
                option {
                    attributes["value"] = BoardType.SANDBOX.name
                    +"Sandbox"
                }
                option {
                    attributes["value"] = BoardType.DEMO.name
                    +"Demo"
                }
                option {
                    attributes["value"] = BoardType.CHOPSHOP.name
                    +"Chop Shop"
                }
            }
        }
        button(classes = "btn btn-primary") { +"Create" }
    }
}
