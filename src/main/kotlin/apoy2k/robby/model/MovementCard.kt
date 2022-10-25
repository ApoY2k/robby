package apoy2k.robby.model

import org.ktorm.entity.Entity
import org.ktorm.schema.*

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

object Cards : Table<MovementCard>("cards") {
    val id = int("id").primaryKey().bindTo { it.id }
    val player = int("player_id").references(Players) { it.player }
    val game = int("game_id").references(Games) { it.game }
    val robot = int("robot_id").references(Robots) { it.robot }
    val movement = enum<Movement>("movement").bindTo { it.movement }
    val priority = int("priority").bindTo { it.priority }
    val register = int("register").bindTo { it.register }
}

interface MovementCard : Entity<MovementCard> {
    companion object : Entity.Factory<MovementCard>()

    val id: Int
    var player: Player
    var game: Game
    var robot: Robot
    var movement: Movement
    var priority: Int
    var register: Int
}

/**
 * true, if the movement on this card contains any amount of steps in a specific direction
 */
fun MovementCard.hasSteps() = setOf(Movement.STRAIGHT, Movement.STRAIGHT_2, Movement.STRAIGHT_3, Movement.BACKWARDS)
    .contains(movement)
