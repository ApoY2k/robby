package apoy2k.robby.model

import java.util.*

data class Game(val id: UUID = UUID.randomUUID()) {
    // Sequence of all recorded moves for replayability
    val movements = mutableListOf<MovementCard>()

    val players = mutableSetOf<Player>()

    val board = Board(
        listOf(
            listOf(Field(), Field(), Field(), Field()),
            listOf(Field(), Field(), Field(), Field()),
            listOf(Field(), Field(), Field(), Field()),
            listOf(Field(), Field(), Field(), Field()),
        )
    )

    val deck = mutableListOf(
        MovementCard(Movement.STRAIGHT, 1),
        MovementCard(Movement.STRAIGHT_2, 2),
        MovementCard(Movement.STRAIGHT_3, 3),
        MovementCard(Movement.BACKWARDS, 4),
        MovementCard(Movement.BACKWARDS_2, 5),
        MovementCard(Movement.BACKWARDS_3, 6),
        MovementCard(Movement.HOLD, 7),
        MovementCard(Movement.TURN_180, 8),
        MovementCard(Movement.TURN_LEFT, 9),
        MovementCard(Movement.TURN_RIGHT, 10),
    ).apply { shuffle() }

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
