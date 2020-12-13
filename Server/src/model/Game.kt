package apoy2k.robby.model

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
}

data class Game(val id: UUID = UUID.randomUUID()) {

    val players = mutableSetOf<Player>()

    val board = Board(
        listOf(
            listOf(Field(), Field(), Field(), Field(), Field(), Field(), Field(), Field()),
            listOf(Field(), Field(), Field(), Field(), Field(), Field(), Field(), Field()),
            listOf(Field(), Field(), Field(), Field(), Field(), Field(), Field(), Field()),
            listOf(Field(), Field(), Field(), Field(), Field(), Field(), Field(), Field()),
            listOf(Field(), Field(), Field(), Field(), Field(), Field(), Field(), Field()),
            listOf(Field(), Field(), Field(), Field(), Field(), Field(), Field(), Field()),
            listOf(Field(), Field(), Field(), Field(), Field(), Field(), Field(), Field()),
            listOf(Field(), Field(), Field(), Field(), Field(), Field(), Field(), Field()),
        )
    )

    val deck = DEFAULT_FULL_DECK.shuffled().toMutableList()

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
}
