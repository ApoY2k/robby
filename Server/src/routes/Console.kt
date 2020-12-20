package apoy2k.robby.routes

import apoy2k.robby.data.Storage
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.channels.SendChannel
import java.util.*

fun Route.console(storage: Storage, viewUpdates: SendChannel<Unit>) {
    route("console") {
        route("/players") {
            get("/list") {
                call.respond(storage.game.players.map {
                    mapOf(
                        "id" to it.id,
                        "name" to it.name,
                        "session" to it.session.id,
                        "robot" to it.robot?.id
                    )
                })
            }
        }

        route("/robots") {
            get("/setdamage") {
                val id = call.parameters["id"]
                val value = call.parameters["value"]?.toInt()

                val robot = storage.game.players.mapNotNull { it.robot }.firstOrNull { it.id == UUID.fromString(id) }

                if (robot == null) {
                    call.respond("Robot not found")
                    return@get
                }

                if (value == null || value < 0 || value > 10) {
                    call.respond("Invalid value")
                    return@get
                }

                robot.damage = value

                viewUpdates.send(Unit)
                call.respond("$robot New damage value: ${robot.damage}")
            }
        }
    }
}
