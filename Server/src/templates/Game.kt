package apoy2k.robby.templates

import apoy2k.robby.*
import apoy2k.robby.engine.Game
import apoy2k.robby.model.JoinGameCommand
import apoy2k.robby.model.PlaceRobotCommand
import apoy2k.robby.model.Board
import apoy2k.robby.model.Player
import apoy2k.robby.model.RobotModel
import kotlinx.html.*

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
    div {
        attributes[ATTR_BIND] = VIEW_PLAYERS

        players.forEach {
            div(classes = "row") {
                div(classes = "col") {
                    it.name
                }
            }
        }
    }
    div(classes = "row") {
        div(classes = "col") {
            form(classes = "form") {
                attributes[ATTR_ACTION] = JoinGameCommand().toString()

                div(classes = "form-group") {
                    input(classes = "form-control", name = CommandField.PLAYER_NAME.name)
                }

                button(classes = "btn btn-primary", type = ButtonType.submit) {
                    +"Join"
                }
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
