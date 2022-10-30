package apoy2k.robby.engine

import apoy2k.robby.exceptions.IncompleteAction
import apoy2k.robby.exceptions.InvalidGameState
import apoy2k.robby.model.*
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.*

class RobotEngine(
    private val database: Database,
) {

    /**
     * Return the card for a robot and register
     */
    fun getRegister(robotId: Int, register: Int) = database.cards
        .find { it.robotId eq robotId and (it.register eq register) }

    /**
     * Write a card into a register of a players robot
     */
    fun selectCard(robot: Robot, register: Int, cardId: Int) {
        val card = database.cards.find { it.id eq cardId }
            ?: throw IncompleteAction("Card with ID '$cardId' does not exist")

        if (card.gameId != robot.gameId) {
            throw IncompleteAction("$robot and $card do not belong to the same game")
        }

        card.robotId = robot.id
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

        database.batchUpdate(MovementCards) {
            cards.values.forEach { card ->
                item {
                    set(it.register, card.register)
                    where { it.id eq card.id }
                }
            }
        }
    }

    /**
     * Prepare a robot for a new round
     */
    fun prepareNewRound(gameId: Int, robot: Robot) {
        clearRegisters(robot)
        robot.toggleReady()

        if (robot.powerDownScheduled) {
            robot.togglePowerDown()
            robot.poweredDown = true
        } else {
            drawCards(gameId, robot)

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
    fun drawCards(gameId: Int, robot: Robot) {
        val cards = database.cards
            .filter { it.gameId eq gameId and it.robotId.isNull() }
            .map { it }
            .shuffled()
            .take(Integer.max(0, 9 - robot.damage))

        database.batchUpdate(MovementCards) {
            cards.forEach { card ->
                item {
                    set(it.robotId, robot.id)
                    where { it.id eq card.id }
                }
            }
        }
    }

    /**
     * Get all cards drawn by a specific robot
     */
    fun getDrawnCards(robotId: Int): List<MovementCard> = database.cards
        .filter { it.robotId eq robotId }
        .map { it }

    /**
     * Createa new robot in a game for a session
     */
    fun createNewRobot(gameId: Int, session: Session, model: RobotModel): Robot {
        if (session.name.isBlank()) throw IncompleteAction("No name provided")
        if (database.robots.any { it.gameId eq gameId and (it.session eq session.id) })
            throw InvalidGameState("$session already has a Robot in this game")

        val robot = Robot.new(model, session).also {
            it.gameId = gameId
        }

        database.robots.add(robot)

        return robot
    }
}
