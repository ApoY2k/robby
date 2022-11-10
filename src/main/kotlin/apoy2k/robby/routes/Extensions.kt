package apoy2k.robby.routes

import apoy2k.robby.model.Session
import apoy2k.robby.model.User
import apoy2k.robby.model.user
import io.ktor.server.application.*
import io.ktor.server.sessions.*
import io.ktor.util.pipeline.*
import org.ktorm.database.Database

/**
 * Get the user object from a request based on the provided session
 */
fun PipelineContext<*, ApplicationCall>.getUser(database: Database): User? {
    val session = call.sessions.get<Session>()
    return database.user(session)
}
