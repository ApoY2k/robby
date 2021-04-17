package apoy2k.robby.model

import apoy2k.robby.exceptions.IncompleteAction
import apoy2k.robby.exceptions.InvalidGameState
import java.util.*

data class Player(val name: String, val session: Session, val id: UUID = UUID.randomUUID()) {
    var robot: Robot? = null

    var drawnCards = emptyList<MovementCard>()
        private set

    var cardsConfirmed = false
        private set

    /**
     * Draw a new set of cards
     */
    fun takeCards(cards: Iterable<MovementCard>) {
        drawnCards = cards.sortedByDescending { it.priority }.onEach { it.player = this }
    }

    /**
     * Select the card with the given id. Card is expected to be "drawn"
     */
    fun selectCard(register: String?, cardId: String?) {
        val id = UUID.fromString(cardId)

        val card = drawnCards.firstOrNull { it.id == id }
            ?: throw InvalidGameState("Card with id [$id] not found")

        val register = register?.toInt()
            ?: throw IncompleteAction("Register [$register] is not a valid register number")

        val robot = robot ?: throw InvalidGameState("Player has no robot assigned")

        robot.setRegister(register, card)
    }

    /**
     * Confirm the current selection of cards
     */
    fun toggleConfirm() {
        if (!cardsConfirmed && robot?.hasAllRegistersFilled() == false) {
            throw InvalidGameState("Not all registers are filled yet")
        }

        cardsConfirmed = !cardsConfirmed
    }

    override fun toString() = "$name:$robot"
}
