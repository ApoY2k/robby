package apoy2k.robby.kotlin.model

import apoy2k.robby.model.Field
import org.junit.jupiter.api.Test

class FieldTest {

    @Test
    fun `unwrap to board`() {
        val fields = listOf(
            Field.new().also {
                it.positionX = 0
                it.positionY = 0
            },
            Field.new().also {
                it.positionX = 1
                it.positionY = 0
            },
            Field.new().also {
                it.positionX = 2
                it.positionY = 0
            },
            Field.new().also {
                it.positionX = 1
                it.positionY = 0
            },
            Field.new().also {
                it.positionX = 1
                it.positionY = 1
            },
            Field.new().also {
                it.positionX = 1
                it.positionY = 2
            },
            Field.new().also {
                it.positionX = 2
                it.positionY = 0
            },
            Field.new().also {
                it.positionX = 2
                it.positionY = 1
            },
            Field.new().also {
                it.positionX = 2
                it.positionY = 2
            },
        )
    }
}
