package apoy2k.robby.templates

import apoy2k.robby.engine.Game
import io.ktor.application.*
import io.ktor.response.*
import kotlinx.html.*
import java.util.*

abstract class ViewComponent {
    internal val id: String = UUID.randomUUID().toString()
    abstract fun render(call: ApplicationCall, parent: FlowContent)

    fun renderView(component: ViewComponent, call: ApplicationCall) {
        DIV(mapOf("class" to "view", "data-view" to id)).render(this)
    }

}

class BoardComponent(private val game: Game) : ViewComponent() {
    override fun render(call: ApplicationCall, parent: FlowContent) {
        parent.div {
            +"hello world"
        }
    }
}

fun HTML.renderView(component: ViewComponent, call: ApplicationCall) {
    body {
        renderView(component, call)
    }
}

suspend fun ApplicationCall.renderView(component: ViewComponent) {
    respond(renderView(component).toString())
}
