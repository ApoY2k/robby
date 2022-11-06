package apoy2k.robby.routes

import apoy2k.robby.engine.fieldAt
import apoy2k.robby.engine.updateLaserOverlays
import apoy2k.robby.model.*
import apoy2k.robby.model.predef.board.generateChopShopBoard
import apoy2k.robby.templates.HomeTpl
import apoy2k.robby.templates.LayoutTpl
import apoy2k.robby.templates.renderBoard
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.*
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.find
import org.ktorm.entity.map
import java.time.Clock

fun Route.base(
    clock: Clock,
    database: Database,
) {
    get(Location.ROOT.path) {
        val session = call.sessions.get<Session>()
        val user = database.users.find { it.id eq (session?.userId ?: -1) }
        val games = database.games.map { it }

        call.respondHtmlTemplate(LayoutTpl(user)) {
            content {
                insert(HomeTpl(clock.instant(), games, user)) {}
            }
        }
    }

    get(Location.BOARDS_ROOT.path) {
        val session = call.sessions.get<Session>()
        val user = database.users.find { it.id eq (session?.userId ?: -1) }

        call.respondHtmlTemplate(LayoutTpl(user)) {
            content {
                div("row") {
                    div("col") {
                        ul {
                            li {
                                a(Location.BOARDS_VIEW.build(mapOf("id" to "chop-shop"))) { +"Chop Shop" }
                            }
                            li {
                                a(Location.BOARDS_VIEW.build(mapOf("id" to "laser-test"))) { +"Laser Render" }
                            }
                            li {
                                a(Location.BOARDS_VIEW.build(mapOf("id" to "robot-states"))) { +"Robot States" }
                            }
                        }
                    }
                }
            }
        }
    }

    get(Location.BOARDS_VIEW.path) {
        val session = call.sessions.get<Session>()
        val user = database.users.find { it.id eq (session?.userId ?: -1) }
        val robots = mutableListOf<Robot>()
        val board = when (call.parameters["id"]) {
            "chop-shop" -> {
                val board = generateChopShopBoard()
                val robot1 = Robot.new(RobotModel.ZIPPY).also {
                    it.facing = Direction.UP
                }
                board.fieldAt(8, 4).robotId = robot1.id
                board.updateLaserOverlays(setOf(robot1))
                board
            }

            else -> null
        }

        if (board == null) {
            call.respondRedirect(Location.BOARDS_ROOT.path)
            return@get
        }

        call.respondHtmlTemplate(LayoutTpl(user)) {
            content {
                h2 {
                    +"Board Preview"
                }
                div {
                    renderBoard(board, robots)
                }
            }
        }
    }
}
