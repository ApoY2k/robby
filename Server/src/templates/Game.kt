package apoy2k.robby.templates

import apoy2k.robby.ATTR_ACTION
import apoy2k.robby.ATTR_BIND
import apoy2k.robby.VIEW_BOARD
import apoy2k.robby.engine.Game
import apoy2k.robby.engine.PlaceRobotCommand
import apoy2k.robby.model.Board
import apoy2k.robby.model.Player
import apoy2k.robby.model.RobotModel
import kotlinx.html.HtmlBlockTag
import kotlinx.html.div

fun HtmlBlockTag.renderBoard(board: Board) {
    div(classes = "row") {
        attributes[ATTR_BIND] = VIEW_BOARD

        div(classes = "col") {
            board.cells.forEach {
                div(classes = "row") {
                    it.forEach {
                        div(classes = "col btn btn-primary field") {
                            val command = PlaceRobotCommand(it.id.toString(), RobotModel.ZIPPY.name)
                            attributes[ATTR_ACTION] = command.toString()

                            +(it.robot?.model?.name ?: "<Place>")
                        }
                    }
                }
            }
        }
    }
}

fun HtmlBlockTag.renderPlayers(players: Set<Player>) {
    players.forEach {
        div(classes = "row") {
            div(classes = "col") {
                it.name
            }
        }
    }
}

fun HtmlBlockTag.renderGame(game: Game) {
    div(classes = "row") {
        div(classes = "col") {
            renderBoard(game.board)
        }
        div(classes = "col-2") {
            renderPlayers(game.players)
        }
    }
}
