package apoy2k.robby.repositories

import apoy2k.robby.model.*
import org.ktorm.database.Database
import org.ktorm.entity.add
import java.time.Clock

class GameRepository(
    private val clock: Clock,
    private val database: Database
) {
    fun createNewGame(type: BoardType): DbGame {
        val game = DbGame {
            state = GameState.PROGRAMMING_REGISTERS
            currentRegister = 1
            startedAt = clock.instant()
            finishedAt = null
        }

        val fields = listOf(
            DbField {
                this.conditions = listOf()
                this.outgoingDirection = listOf(Direction.DOWN, Direction.LEFT)
                this.incomingDirections = Direction.NONE
                this.positionX = 0
                this.positionY = 0
                this.type = FieldType.NONE
                this.game = game
            }
        )

        return database.useTransaction {
            database.games.add(game)
            fields
                .forEach {
                    database.fields.add(it)
                }
            game
        }
    }
}
