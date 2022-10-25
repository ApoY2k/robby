package apoy2k.robby.engine

import apoy2k.robby.exceptions.IncompleteAction
import apoy2k.robby.model.Cards
import apoy2k.robby.model.MovementCard
import apoy2k.robby.model.Player
import apoy2k.robby.model.cards
import org.ktorm.database.Database
import org.ktorm.dsl.batchUpdate
import org.ktorm.dsl.eq
import org.ktorm.entity.find
import org.ktorm.entity.update

class PlayerEngine(
    private val database: Database
) {

    /**
     * Assign a set of cards to a player
     */
    fun takeCards(player: Player, cards: Iterable<MovementCard>) {
        database.batchUpdate(Cards) {
            cards.onEach {
                item {
                    set(it.player, player.id)
                }
            }
        }
    }

    /**
     * Write a card into a register of a players robot
     */
    fun selectCard(player: Player, register: Int, cardId: Int) {
        val robot = player.robot ?: throw IncompleteAction("$player has no robot")

        val card = database.cards.find { it.id eq cardId }
            ?: throw IncompleteAction("Card with ID '$cardId' does not exist")

        if (card.player != player) {
            throw IncompleteAction("$card does not belong to $player")
        }

        if (card.game != player.game) {
            throw IncompleteAction("$player and $card do not belong to the same game")
        }

        card.robot = robot
        card.register = register
        database.cards.update(card)
    }

}
