package apoy2k.robby.routes

import apoy2k.robby.model.Session
import apoy2k.robby.model.User
import apoy2k.robby.model.user
import io.ktor.server.routing.RoutingContext
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import org.ktorm.database.Database

/**
 * Get the user object from a request based on the provided session
 */
fun RoutingContext.getUser(database: Database): User? {
    val session = call.sessions.get<Session>()
    return database.user(session)
}
