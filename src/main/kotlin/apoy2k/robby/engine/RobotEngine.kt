package apoy2k.robby.engine

import apoy2k.robby.exceptions.IncompleteAction
import apoy2k.robby.exceptions.InvalidGameState
import apoy2k.robby.model.*
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.*
import org.slf4j.LoggerFactory

class RobotEngine(
    private val database: Database,
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

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

        logger.info("Selecting card $cardId for $robot to register $register")

        card.robotId = robot.id
        card.register = register
        database.useTransaction { tx ->
            database.update(MovementCards) {
                set(it.register, null)
                where { it.robotId eq robot.id and (it.register eq register) }
            }
            database.cards.update(card)
            tx.commit()
        }
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
                cards[1]?.robotId = null
            }

            if (robot.damage <= 7) {
                cards[2]?.register = null
                cards[2]?.robotId = null
            }

            if (robot.damage <= 6) {
                cards[3]?.register = null
                cards[3]?.robotId = null
            }

            if (robot.damage <= 5) {
                cards[4]?.register = null
                cards[4]?.robotId = null
            }

            if (robot.damage <= 4) {
                cards[5]?.register = null
                cards[5]?.robotId = null
            }
        } else {
            cards.forEach { it.value.register = null }
        }

        database.batchUpdate(MovementCards) {
            cards.values.forEach { card ->
                item {
                    set(it.register, card.register)
                    set(it.robotId, card.robotId)
                    where { it.id eq card.id }
                }
            }
        }
    }

    /**
     * Prepare a robot for a new round
     */
    fun prepareNewRound(gameId: Int, robot: Robot) {
        toggleReady(robot)
        clearRegisters(robot)

        if (robot.powerDownScheduled) {
            togglePowerDown(robot)
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

        database.useTransaction { tx ->
            database.update(MovementCards) {
                set(it.robotId, null)
                where { it.robotId eq robot.id and it.register.isNull() }
            }
            database.batchUpdate(MovementCards) {
                cards.forEach { card ->
                    item {
                        set(it.robotId, robot.id)
                        where { it.id eq card.id }
                    }
                }
            }
            tx.commit()
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
    fun createNewRobot(gameId: Int, name: String, userId: Int, model: RobotModel): Robot {
        if (name.isBlank()) throw IncompleteAction("No name provided")
        if (database.robots.any { it.gameId eq gameId and (it.userId eq userId) })
            throw InvalidGameState("$name already has a Robot in this game")

        val robot = Robot.new(model, name, userId).also {
            it.gameId = gameId
        }

        database.robots.add(robot)

        return robot
    }

    /**
     * Toggle the ready state on a robot
     */
    fun toggleReady(robot: Robot) {
        robot.ready = !robot.ready
        database.robots.update(robot)
    }

    /**
     * Toggle the powerdown scheduled state on a robot
     */
    fun togglePowerDown(robot: Robot) {
        robot.powerDownScheduled = !robot.powerDownScheduled
        database.robots.update(robot)
    }
}
