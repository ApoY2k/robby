package apoy2k.robby.routes

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
import org.ktorm.entity.map

fun Route.base(
    database: Database,
) {
    get(Location.ROOT.path) {
        val session = call.sessions.get<Session>()
        val games = database.games.map { it }

        call.respondHtmlTemplate(LayoutTpl(session)) {
            content {
                insert(HomeTpl(games, session)) {}
            }
        }
    }

    get(Location.BOARDS_ROOT.path) {
        val session = call.sessions.get<Session>()

        call.respondHtmlTemplate(LayoutTpl(session)) {
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
        val robots = mutableListOf<Robot>()
        val board = when (call.parameters["id"]) {
            "chop-shop" -> generateChopShopBoard()
            "laser-test" -> listOf(
                listOf(Field.new(), Field.new(FieldType.WALL, Direction.LEFT), Field.new()),
                listOf(Field.new(FieldType.WALL, Direction.DOWN), Field.new(), Field.new()),
                listOf(Field.new(), Field.new(), Field.new(FieldType.WALL, Direction.RIGHT)),
                listOf(Field.new(), Field.new(FieldType.WALL, Direction.UP), Field.new()),
            )

            "robot-states" -> {
                val board = listOf(
                    listOf(Field.new(), Field.new()),
                    listOf(Field.new(), Field.new()),
                )
                val robot1 = Robot.new(RobotModel.HUZZA).also {
                    it.id = 1
                    it.damage = 1
                    it.passedCheckpoints = 2
                }
                robots.add(robot1)
                val robot2 = Robot.new(RobotModel.GEROG).also {
                    it.id = 2
                    it.damage = 1
                    it.passedCheckpoints = 2
                    it.poweredDown = true
                }
                robots.add(robot2)
                board[0][1].robotId = robot1.id
                board[1][1].robotId = robot2.id
                board
            }

            else -> null
        }

        if (board == null) {
            call.respondRedirect(Location.BOARDS_ROOT.path)
            return@get
        }

        call.respondHtmlTemplate(LayoutTpl(session)) {
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
