package apoy2k.robby.model

import apoy2k.robby.model.predef.board.generateChopShopBoard
import apoy2k.robby.model.predef.deck.STANDARD_DECK
import java.util.*

enum class GameState {
    PROGRAMMING_REGISTERS,
    POWER_DOWN,
    EXECUTING_REGISTER_1,
    EXECUTING_REGISTER_2,
    EXECUTING_REGISTER_3,
    EXECUTING_REGISTER_4,
    EXECUTING_REGISTER_5,
    MOVE_BARD_ELEMENTS,
    FIRE_LASERS,
    CHECKPOINTS,
    REPAIR_POWERUPS,
    FINISHED
}

data class Game(val id: String = UUID.randomUUID().toString()) {

    val players = mutableSetOf<Player>()

    val board = Board(generateChopShopBoard())

    val deck = STANDARD_DECK.shuffled().toMutableList()

    var state: GameState = GameState.PROGRAMMING_REGISTERS

    /**
     * Find the player associated with a session
     */
    fun playerFor(session: Session?): Player? {
        return players.firstOrNull { it.session == session }
    }

    /**
     * Check if a specific session has joined this game ( = is associated with a player)
     */
    fun hasJoined(session: Session?): Boolean {
        return playerFor(session) != null
    }

    override fun toString(): String {
        return "Game(id=$id)"
    }
}
