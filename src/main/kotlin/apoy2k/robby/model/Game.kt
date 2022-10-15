package apoy2k.robby.model

import apoy2k.robby.model.predef.deck.generateStandardDeck
import org.apache.commons.lang3.RandomStringUtils

enum class GameState {
    PROGRAMMING_REGISTERS,
    EXECUTING_REGISTER_1,
    EXECUTING_REGISTER_2,
    EXECUTING_REGISTER_3,
    EXECUTING_REGISTER_4,
    EXECUTING_REGISTER_5,
    MOVE_BARD_ELEMENTS,
    FIRE_LASERS,
    CHECKPOINTS,
    REPAIR_POWERUPS
}

data class Game(val id: String = RandomStringUtils.randomAlphanumeric(5)) {
    val players = mutableSetOf<Player>()
    val deck = generateStandardDeck()
    var state = GameState.PROGRAMMING_REGISTERS
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
