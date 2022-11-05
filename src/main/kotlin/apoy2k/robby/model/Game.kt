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
    FIRE_ROBOT_LASERS,
    CHECKPOINTS,
    REPAIR_POWERUPS
}

@Suppress("unused")
object Games : Table<Game>("games") {
    val id = int("id").primaryKey().bindTo { it.id }
    val currentRegister = int("currentRegister").bindTo { it.currentRegister }
    val state = enum<GameState>("state").bindTo { it.state }
    val startedAt = timestamp("startedAt").bindTo { it.startedAt }
    val finishedAt = timestamp("finishedAt").bindTo { it.finishedAt }
}

interface Game : Entity<Game> {
    companion object : Entity.Factory<Game>()

    var id: Int
    var currentRegister: Int
    var state: GameState
    var startedAt: Instant?
    var finishedAt: Instant?
}

/**
 * true, if the game has finished after the given instant or no finish date is set
 */
fun Game.isFinished(now: Instant) = when (finishedAt) {
    null -> false
    else -> now.isAfter(finishedAt)
}

/**
 * true, if the game has started after the given instant or no start is set
 */
fun Game.hasStarted(now: Instant) = when (startedAt) {
    null -> false
    else -> now.isAfter(startedAt)
}
