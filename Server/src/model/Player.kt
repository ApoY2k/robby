package apoy2k.robby.model

import apoy2k.robby.exceptions.IncompleteAction
import apoy2k.robby.exceptions.InvalidGameState
import java.util.*

data class Player(val name: String, val session: Session, val id: UUID = UUID.randomUUID()) {
    var robot: Robot? = null

    val selectedCards = mutableListOf<MovementCard>()

    var drawnCards = emptyList<MovementCard>()
        private set

    var cardsConfirmed = false
        private set

    /**
     * Draw a new set of cards
     */
    fun takeCards(cards: List<MovementCard>) {
        drawnCards = cards.onEach { it.player = this }
    }

    /**
     * Select the card with the given id. Card is expected to be "drawn"
     */
    fun selectCard(cardId: String?) {
        if (cardId.isNullOrBlank()) {
            throw IncompleteAction("cardId missing")
        }

        val id = UUID.fromString(cardId)
        val card = drawnCards.firstOrNull { it.id == id } ?: throw InvalidGameState("Card with id [$id] not found")

        if (selectedCards.contains(card)) {
            selectedCards.remove(card)
        } else {
            if (selectedCards.count() < 3) {
                selectedCards.add(card)
            }
        }
    }

    /**
     * Confirm the current selection of cards
     */
    fun confirmCards() {
        if (selectedCards.count() < 3) {
            throw InvalidGameState("Must choose three cards before confirming")
        }

        cardsConfirmed = !cardsConfirmed
    }
}
