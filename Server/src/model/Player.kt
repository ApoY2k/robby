package apoy2k.robby.model

import apoy2k.robby.VIEW_CARDS
import apoy2k.robby.exceptions.IncompleteCommand
import apoy2k.robby.exceptions.InvalidGameState
import java.util.*

data class Player(val name: String, val session: Session) {
    val drawPile = mutableListOf<MovementCard>()
    val drawnCards = mutableListOf<MovementCard>()
    val discardPile = mutableListOf<MovementCard>()
    val selectedCards = mutableListOf<MovementCard>()
    var robot: Robot? = null

    fun drawCards() {
        drawPile.removeIf {
            if (drawnCards.count() < 5) {
                drawnCards.add(it)
                true
            } else {
                false
            }
        }
    }

    fun selectCard(cardId: String?) {
        if (cardId.isNullOrBlank()) {
            throw IncompleteCommand("cardId missing")
        }

        val id = UUID.fromString(cardId)
        val card = drawnCards.firstOrNull { it.id == id } ?: throw InvalidGameState("Card with id [$id] not found")

        if (selectedCards.contains(card)) {
            selectedCards.remove(card)
        } else {
            selectedCards.add(card)
        }
    }
}
