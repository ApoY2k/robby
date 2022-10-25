package apoy2k.robby.model.predef.board

import apoy2k.robby.model.Field
import apoy2k.robby.model.Game

fun linkBoard(game: Game, fields: List<List<Field>>) = fields
    .mapIndexed { row, rowFields ->
        rowFields.mapIndexed { col, field ->
            field.also {
                it.game = game
                it.positionX = col
                it.positionY = row
            }
        }
    }
    .flatten()
