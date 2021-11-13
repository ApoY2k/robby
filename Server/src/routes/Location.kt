package apoy2k.robby.routes

import io.ktor.locations.*
import java.lang.reflect.Type
import java.util.*

enum class Location(val path: String) {
    HOME("/"),
    SET_USERNAME("/set-username"),
    GAME("/game"),
    VIEW_GAME("/game/{id}");

    fun build(params: Map<String, String> = mapOf()) = params.entries
        .fold(this.path) { acc, param ->
            acc.replace("{${param.key}}", param.value)
        }
}
