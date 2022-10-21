package apoy2k.robby.model

import org.apache.commons.lang3.RandomStringUtils
import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.enum
import org.ktorm.schema.long
import org.ktorm.schema.short

enum class Movement {
    STAY,
    STRAIGHT,
    STRAIGHT_2,
    STRAIGHT_3,
    TURN_LEFT,
    TURN_RIGHT,
    TURN_180,
    BACKWARDS,
}

interface DbCard : Entity<DbCard> {
    companion object : Entity.Factory<DbCard>()

    val id: Long
    var player: DbPlayer
    var game: DbGame
    var robot: DbRobot
    var movement: Movement
    var priority: Short
    var register: Short
}

object Cards : Table<DbCard>("cards") {
    val id = long("id").primaryKey().bindTo { it.id }
    val player = long("player_id").references(Players) { it.player }
    val game = long("game_id").references(Games) { it.game }
    val robot = long("robot_id").references(Robots) { it.robot }
    val movement = enum<Movement>("movement").bindTo { it.movement }
    val priority = short("priority").bindTo { it.priority }
    val register = short("register").bindTo { it.register }
}

data class MovementCard(val movement: Movement, val priority: Int) {
    val id: String = RandomStringUtils.randomAlphanumeric(5)
    var player: Player? = null

    // true, if the movement on this card contains any amount of steps in a specific direction
    val hasSteps = setOf(
        Movement.STRAIGHT, Movement.STRAIGHT_2, Movement.STRAIGHT_3, Movement.BACKWARDS
    ).contains(movement)

    override fun toString() = "MovementCard($movement, $priority)"
}
