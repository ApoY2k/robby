package apoy2k.robby.routes

import apoy2k.robby.data.Storage
import apoy2k.robby.model.*
import apoy2k.robby.model.predef.board.generateChopShopBoard
import apoy2k.robby.templates.HomeTpl
import apoy2k.robby.templates.LayoutTpl
import apoy2k.robby.templates.renderBoard
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.*
import org.slf4j.LoggerFactory

fun Route.base(storage: Storage) {
    val logger = LoggerFactory.getLogger("${this.javaClass.name}.base")

    get("/") {
        call.respondHtmlTemplate(LayoutTpl()) {
            content {
                insert(HomeTpl(storage, call.sessions.get())) {}
            }
        }
    }

    post(Location.SET_USERNAME.path) {
        val form = call.receiveParameters()
        val name = form["username"] ?: ""
        val session = call.sessions.get<Session>()
        logger.debug("Saving username [$name] for $session")
        call.sessions.set(session?.copy(name = name))
        call.respondRedirect("/")
    }

    get(Location.BOARDS_ROOT.path) {
        call.respondHtmlTemplate(LayoutTpl()) {
            content {
                div("row") {
                    div("col") {
                        ul {
                            li {
                                a(Location.BOARDS_VIEW.build(mapOf("id" to "chop-shop"))) {
                                    +"Chop Shop"
                                }
                            }
                            li {
                                a(Location.BOARDS_VIEW.build(mapOf("id" to "laser-test"))) {
                                    +"Laser Render"
                                }
                            }
                            li {
                                a(Location.BOARDS_VIEW.build(mapOf("id" to "robot-states"))) {
                                    +"Robot States"
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    get(Location.BOARDS_VIEW.path) {
        val board = when (call.parameters["id"]) {
            "chop-shop" -> Board(generateChopShopBoard())
            "laser-test" -> Board(
                listOf(
                    listOf(Field(), Field(FieldType.WALL, Direction.LEFT), Field()),
                    listOf(Field(FieldType.WALL, Direction.DOWN), Field(), Field()),
                    listOf(Field(), Field(), Field(FieldType.WALL, Direction.RIGHT)),
                    listOf(Field(), Field(FieldType.WALL, Direction.UP), Field()),
                ),
            )

            "robot-states" -> {
                val board = Board(
                    listOf(
                        listOf(Field(), Field()),
                        listOf(Field(), Field()),
                    )
                )
                val robot1 = Robot(RobotModel.HUZZA)
                robot1.damage = 1
                robot1.passedCheckpoints = 2
                val robot2 = Robot(RobotModel.GEROG)
                robot2.damage = 1
                robot2.passedCheckpoints = 2
                robot2.poweredDown = true
                board.fieldAt(0, 0).robot = robot1
                board.fieldAt(1, 1).robot = robot2
                board
            }

            else -> null
        }

        if (board == null) {
            call.respondRedirect(Location.BOARDS_ROOT.path)
            return@get
        }

        call.respondHtmlTemplate(LayoutTpl()) {
            content {
                h2 {
                    +"Board Preview"
                }
                div {
                    renderBoard(board)
                }
            }
        }
    }
}
