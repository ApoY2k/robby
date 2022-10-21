package apoy2k.robby.model

import apoy2k.robby.exceptions.IncompleteAction
import apoy2k.robby.exceptions.InvalidGameState
import org.apache.commons.lang3.RandomStringUtils
import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.boolean
import org.ktorm.schema.long
import org.ktorm.schema.varchar

interface DbPlayer : Entity<DbPlayer> {
    companion object : Entity.Factory<DbPlayer>()

    val id: Long
    var sessionId: String
    var robot: DbRobot
    var name: String
    var cards: List<MovementCard>
    var ready: Boolean
    var powerDownScheduled: Boolean
}

object Players : Table<DbPlayer>("players") {
    val id = long("id").primaryKey().bindTo { it.id }
    val sessionId = varchar("sessionId").bindTo { it.sessionId }
    val robot = long("robot_id").references(Robots) { it.robot }
    val name = varchar("name").bindTo { it.name }
    val ready = boolean("ready").bindTo { it.ready }
    val powerDownScheduled = boolean("powerDownScheduled").bindTo { it.powerDownScheduled }
}

data class Player(
    val name: String,
    val robot: Robot,
    val session: Session,
    val id: String = RandomStringUtils.randomAlphanumeric(5)
) {

    // Drawn cards to choose for registers
    var drawnCards = emptyList<MovementCard>()
        private set

    // Player is ready for the turn to start
    var ready = false
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

        robot.setRegister(register, card)
    }

    /**
     * Toggle power down for the round
     */
    fun togglePowerDown() {
        powerDownScheduled = !powerDownScheduled
    }

    /**
     * Toggle ready state
     */
    fun toggleReady() {
        ready = !ready
    }

    override fun toString() = "Player($name, $robot)"
}
