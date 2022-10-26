package apoy2k.robby.engine

import apoy2k.robby.exceptions.IncompleteAction
import apoy2k.robby.model.*
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.*

class RobotEngine(
    private val database: Database,
) {

    /**
     * Assign a set of cards to a player
     */
    fun takeCards(robot: Robot, cards: Iterable<MovementCard>) {
        database.batchUpdate(Cards) {
            cards.onEach {
                item {
                    set(it.robotId, robot.id)
                }
            }
        }
    }

    /**
     * Write a card into a register of a players robot
     */
    fun selectCard(robot: Robot, register: Int, cardId: Int) {
        val card = database.cards.find { it.id eq cardId }
            ?: throw IncompleteAction("Card with ID '$cardId' does not exist")

        if (card.game != robot.game) {
            throw IncompleteAction("$robot and $card do not belong to the same game")
        }

        card.robot = robot
        card.register = register
        database.cards.update(card)
    }

    /**
     * Clear all registers of a robot.
     * @param respectDamageLock If true, will not clear registers that should be locked based
     * on the current damage value of this robot
     */
    fun clearRegisters(robot: Robot, respectDamageLock: Boolean = true) {
        val cards = database.cards
            .filter { it.robotId eq robot.id and it.register.isNotNull() }
            .associateBy { it.register }

        if (respectDamageLock) {
            if (robot.damage <= 8) {
                cards[1]?.register = null
            }

            if (robot.damage <= 7) {
                cards[2]?.register = null
            }

            if (robot.damage <= 6) {
                cards[3]?.register = null
            }

            if (robot.damage <= 5) {
                cards[4]?.register = null
            }

            if (robot.damage <= 4) {
                cards[5]?.register = null
            }
        } else {
            cards.forEach { it.value.register = null }
        }

        database.batchUpdate(Cards) {
            cards.onEach { entry ->
                item {
                    set(it.register, entry.value.register)
                }
            }
        }
    }

    /**
     * Returns true if the robot has movement cards in all registers
     */
    fun hasAllRegistersFilled(robot: Robot) = database.cards.count { it.robotId eq robot.id } == 5

    /**
     * Prepare a robot for a new round
     */
    fun prepareNewRound(game: Game, robot: Robot) {
        clearRegisters(robot)
        robot.toggleReady()

        if (robot.powerDownScheduled) {
            robot.togglePowerDown()
            robot.poweredDown = true
        } else {
            drawCards(game, robot)

            with(robot) {
                if (poweredDown) {
                    damage = 0
                    poweredDown = false
                }
            }
        }
    }

    /**
     * Draw a new set of cards for a robot
     */
    fun drawCards(game: Game, robot: Robot) {
        val drawnCards = game.deck.take(Integer.max(0, 9 - robot.damage))
        game.deck.removeAll(drawnCards)
        player.takeCards(drawnCards)
    }

}
