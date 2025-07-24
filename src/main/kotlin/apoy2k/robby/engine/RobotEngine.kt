package apoy2k.robby.engine

import apoy2k.robby.model.Direction
import apoy2k.robby.model.MovementCards
import apoy2k.robby.model.Robot
import apoy2k.robby.model.RobotModel
import apoy2k.robby.model.add
import apoy2k.robby.model.card
import apoy2k.robby.model.cardCountForRobotRegister
import apoy2k.robby.model.cardsForRobotNoRegister
import apoy2k.robby.model.cardsWithoutRobot
import apoy2k.robby.model.update
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.batchUpdate
import org.ktorm.dsl.eq
import org.ktorm.dsl.isNull
import org.ktorm.dsl.update
import org.ktorm.entity.associateBy
import org.slf4j.LoggerFactory

class RobotEngine(
    private val database: Database,
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Write a card into a register of a players robot
     */
    fun selectCard(robot: Robot, register: Int, cardId: Int) {
        val card = database.card(cardId)
            ?: throw Exception("Card with ID '$cardId' does not exist")

        if (card.gameId != robot.gameId) {
            throw Exception("$robot and $card do not belong to the same game")
        }

        if (robot.isLocked(register)) {
            return
        }

        logger.debug("Selecting card {} for {} to register {}", cardId, robot, register)

        card.robotId = robot.id
        card.register = register
        database.useTransaction {
            database.update(MovementCards) {
                set(it.register, null)
                where { it.robotId eq robot.id and (it.register eq register) }
            }
            database.update(card)
        }
    }

    /**
     * Clear all registers of a robot.
     * @param respectDamageLock If true, will not clear registers that should be locked based
     * on the current damage value of this robot
     */
    fun clearRegisters(robot: Robot, respectDamageLock: Boolean = true) {
        val cards = database.cardsForRobotNoRegister(robot.id).associateBy { it.register }

        // Remove cards from registers that are not locked, as by the register locking rules
        // by setting the register for each card to null, "freeing" them from it

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

        database.update(robot)
    }

    /**
     * Draw a new set of cards for a robot
     */
    fun drawCards(gameId: Int, robot: Robot) {
        val cards = database.cardsWithoutRobot(gameId)
            .shuffled()
            .take(Integer.max(0, 9 - robot.damage))

        database.useTransaction { tx ->
            database.update(MovementCards) {
                set(it.robotId, null)
                where { it.robotId eq robot.id and it.register.isNull() }
                // Only remove cards that are not in registers, as when they're assigned to registers they *must*
                // stay assigned to a robot, too. Cards in a register without a robot don't make any sense
            }
            if (!cards.isEmpty()) {
                database.batchUpdate(MovementCards) {
                    cards.forEach { card ->
                        item {
                            set(it.robotId, robot.id)
                            where { it.id eq card.id }
                        }
                    }
                }
            }
            tx.commit()
        }
    }

    /**
     * Createa new robot in a game for a session
     */
    fun createNewRobot(gameId: Int, name: String, userId: Int, model: RobotModel, facing: Direction): Robot {
        val robot = Robot.new(model, name, userId).also {
            it.gameId = gameId
            it.facing = facing
        }

        database.add(robot)

        return robot
    }

    /**
     * Toggle the ready state on a robot
     */
    fun toggleReady(robot: Robot) {
        if (database.cardCountForRobotRegister(robot.id) != 5) {
            // Robot does not have all registers programmed yet
            return
        }

        robot.ready = !robot.ready
        database.update(robot)
    }

    /**
     * Toggle the powerdown scheduled state on a robot
     */
    fun togglePowerDown(robot: Robot) {
        robot.powerDownScheduled = !robot.powerDownScheduled
        database.update(robot)
    }
}
