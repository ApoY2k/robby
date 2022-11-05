package apoy2k.robby.model

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.enum
import org.ktorm.schema.int

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

@Suppress("unused")
object MovementCards : Table<MovementCard>("movementCards") {
    val id = int("id").primaryKey().bindTo { it.id }
    val gameId = int("game_id").bindTo { it.gameId }
    val robotId = int("robot_id").bindTo { it.robotId }
    val movement = enum<Movement>("movement").bindTo { it.movement }
    val priority = int("priority").bindTo { it.priority }
    val register = int("register").bindTo { it.register }
}

interface MovementCard : Entity<MovementCard> {
    companion object : Entity.Factory<MovementCard>() {
        @JvmStatic
        fun new(movement: Movement, priority: Int) = MovementCard {
            this.movement = movement
            this.priority = priority
        }
    }

    var id: Int
    var gameId: Int
    var robotId: Int?
    var movement: Movement
    var priority: Int
    var register: Int?
}

/**
 * true, if the movement on this card contains any amount of steps in a specific direction
 */
fun MovementCard.hasSteps() = setOf(Movement.STRAIGHT, Movement.STRAIGHT_2, Movement.STRAIGHT_3, Movement.BACKWARDS)
    .contains(movement)
