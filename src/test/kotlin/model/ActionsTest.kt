package apoy2k.robby.kotlin.model

import apoy2k.robby.exceptions.UnknownAction
import apoy2k.robby.model.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ActionsTest {

    @Test
    fun `deserialize action`() {
        val game = Game()
        assertEquals(
            JoinGameAction(RobotModel.ZIPPY.name).also { it.game = game },
            Action.deserializeFromSocket(
                game,
                "${ActionField.LABEL}=${ActionLabel.JOIN_GAME}&" +
                        "${ActionField.ROBOT_MODEL}=${RobotModel.ZIPPY.name}"
            )
        )
    }

    @Test
    fun `deserialize action with wrong syntax fails`() {
        val game = Game()
        assertFailsWith(UnknownAction::class) {
            Action.deserializeFromSocket(game, "${ActionField.LABEL}=${ActionLabel.JOIN_GAME}_player1")
        }
    }

    @Test
    fun `deserialize unknown action fails`() {
        val game = Game()
        assertFailsWith(UnknownAction::class) {
            Action.deserializeFromSocket(game, "test")
        }
    }
}
