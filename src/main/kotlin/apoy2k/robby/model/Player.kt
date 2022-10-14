package apoy2k.robby.model

import apoy2k.robby.exceptions.IncompleteAction
import apoy2k.robby.exceptions.InvalidGameState
import org.apache.commons.lang3.RandomStringUtils

data class Player(val name: String, val session: Session, val id: String = RandomStringUtils.randomAlphanumeric(5)) {
    var robot: Robot? = null

    var drawnCards = emptyList<MovementCard>()
        private set

    var cardsConfirmed = false
        private set

    var powerDownScheduled = false
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
    fun selectCard(register: Int?, cardId: String?) {
        val card = drawnCards.firstOrNull { it.id == cardId }
            ?: throw InvalidGameState("Card with id [$id] not found")

        if (register == null) {
            throw IncompleteAction("Invalid register number")
        }

        val robot = robot ?: throw InvalidGameState("Player has no robot assigned")

        robot.setRegister(register, card)
    }

    /**
     * Toggle power down for the round
     */
    fun togglePowerDown() {
        powerDownScheduled = !powerDownScheduled
    }

    /**
     * Confirm the current selection of cards
     */
    fun toggleConfirm() {
        cardsConfirmed = !cardsConfirmed
    }

    override fun toString() = "Player($name, $robot)"
}
