package apoy2k.robby.routes

import apoy2k.robby.isAuthenticated
import apoy2k.robby.model.Session
import apoy2k.robby.model.User
import apoy2k.robby.model.users
import apoy2k.robby.templates.LayoutTpl
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.*
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.find
import java.util.*

fun Route.auth(
    database: Database,
) {
    get(Location.AUTH.path) {
        val session = call.sessions.get<Session>()
        val user = database.users.find { it.id eq (session?.userId ?: -1) }

        call.respondHtmlTemplate(LayoutTpl(user)) {
            content {
                div("row") {
                    div("col") {
                        p("alert alert-info") {
                            +"This form serves as both login and signup. If the username you choose already exists"
                            +" it is assumed you're trying to login to that account. If not, a new account will be"
                            +" created with the chosen password."
                        }
                        val action = Location.AUTH.build()
                        form(action, method = FormMethod.post) {
                            div("row") {
                                div("col") {
                                    div("input-group") {
                                        span("input-group-text") { +"Username" }
                                        input(InputType.text, name = "username", classes = "form-control")
                                    }
                                    div("input-group") {
                                        span("input-group-text") { +"Password" }
                                        input(InputType.password, name = "password", classes = "form-control")
                                    }
                                }
                                div("col") {
                                    button(classes = "btn btn-primary") { +"Join" }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    post(Location.AUTH.path) {
        val form = call.receiveParameters()
        val name = form["username"] ?: ""
        if (name.isBlank()) {
            throw Exception("Username must not be blank")
        }

        val passwordChallenge = form["password"] ?: ""
        if (passwordChallenge.isBlank()) {
            throw Exception("Password must not be blank")
        }

        var user = database.users.find { it.name eq name }
        if (user == null) {
            user = User.new(name, passwordChallenge)
            database.users.add(user)
        } else {
            val password = Base64.getDecoder().decode(user.password)
            val salt = Base64.getDecoder().decode(user.salt)
            if (!isAuthenticated(passwordChallenge, password, salt)) {
                throw Exception("Wrong password for ${user.name}")
            }
        }

        val session = call.sessions.get() ?: Session()
        call.sessions.set(
            session.copy(userId = user.id)
        )
        call.respondRedirect(Location.ROOT.path)
    }
}
