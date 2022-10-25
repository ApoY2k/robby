package apoy2k.robby.model

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.enum
import org.ktorm.schema.int
import org.ktorm.schema.timestamp
import java.time.Instant

enum class GameState {
    PROGRAMMING_REGISTERS,
    EXECUTING_REGISTERS,
    MOVE_BARD_ELEMENTS_2,
    MOVE_BARD_ELEMENTS_1,
    FIRE_LASERS_2,
    FIRE_LASERS_1,
    CHECKPOINTS,
    REPAIR_POWERUPS
}

object Games : Table<Game>("games") {
    val id = int("id").primaryKey().bindTo { it.id }
    val currentRegister = int("currentRegister").bindTo { it.currentRegister }
    val state = enum<GameState>("state").bindTo { it.state }
    val startedAt = timestamp("startedAt").bindTo { it.startedAt }
    val finishedAt = timestamp("finishedAt").bindTo { it.finishedAt }
}

interface Game : Entity<Game> {
    companion object : Entity.Factory<Game>()

    val id: Int
    var currentRegister: Int
    var state: GameState
    var startedAt: Instant?
    var finishedAt: Instant?
}

fun Game.isFinished(now: Instant) = now.isAfter(this.finishedAt)

fun Game.hasStarted(now: Instant) = now.isAfter(this.startedAt)
