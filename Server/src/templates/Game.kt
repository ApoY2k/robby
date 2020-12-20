package apoy2k.robby.templates

import apoy2k.robby.data.Storage
import apoy2k.robby.model.Session
import kotlinx.html.HtmlBlockTag
import kotlinx.html.div
import kotlin.collections.set

fun HtmlBlockTag.renderGame(storage: Storage, session: Session?) {
    div {
        attributes["id"] = "gameview"

        div(classes = "row") {
            div(classes = "col") {
                renderBoard(storage.game, session)
            }

            div(classes = "col-2") {
                renderPlayers(storage.game, session)
                renderJoinForm(storage.game, session)
            }
        }

        renderProfile(storage.game, session)
    }
}
