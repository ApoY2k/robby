package apoy2k.robby.model

import apoy2k.robby.model.predef.deck.generateStandardDeck
import org.apache.commons.lang3.RandomStringUtils
import org.ktorm.entity.Entity
import org.ktorm.schema.*
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

interface DbGame : Entity<DbGame> {
    companion object : Entity.Factory<DbGame>()

    val id: Long
    var currentRegister: Short
    var state: GameState
    var startedAt: Instant?
    var finishedAt: Instant?
}

object Games : Table<DbGame>("games") {
    val id = long("id").primaryKey().bindTo { it.id }
    val currentRegister = short("currentRegister").bindTo { it.currentRegister }
    val state = enum<GameState>("state").bindTo { it.state }
    val startedAt = timestamp("startedAt").bindTo { it.startedAt }
    val finishedAt = timestamp("finishedAt").bindTo { it.finishedAt }
}

data class Game(val id: String = RandomStringUtils.randomAlphanumeric(5)) {
    val players = mutableSetOf<Player>()
    val deck = generateStandardDeck()
    var state = GameState.PROGRAMMING_REGISTERS
    var currentRegister = 1
    var isFinished = false
    var hasStarted = false

    var board = Board(emptyList())
        private set

    /**
     * Load in a board to this game
     */
    fun loadBoard(board: Board) {
        this.board = board
    }

    /**
     * Find the player associated with a session
     */
    fun playerFor(session: Session?) = players.firstOrNull { it.session == session }

    /**
     * Check if a specific session has joined this game ( = is associated with a player)
     */
    fun hasJoined(session: Session?) = playerFor(session) != null

    override fun toString() = "Game($id)"
}
