package apoy2k.robby.model

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ActionsTest {

    @Test
    fun `deserialize action`() {
        val game = Game()
        assertEquals(
            Action.joinGame(RobotModel.ZIPPY, Direction.DOWN).also { it.game = game },
            Action.deserializeFromSocket(
                "${ActionField.LABEL}=${ActionLabel.JOIN_GAME}&" +
                        "${ActionField.ROBOT_MODEL}=${RobotModel.ZIPPY.name}&" +
                        "${ActionField.ROBOT_FACING}=${Direction.DOWN.name}"
            )
        )
    }

    @Test
    fun `deserialize action with wrong syntax fails`() {
        assertFailsWith(IllegalArgumentException::class) {
            Action.deserializeFromSocket("${ActionField.LABEL}=${ActionLabel.JOIN_GAME}_player1")
        }
    }

    @Test
    fun `deserialize unknown action fails`() {
        assertFailsWith(IllegalArgumentException::class) {
            Action.deserializeFromSocket("test")
        }
    }
}
