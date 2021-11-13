package apoy2k.robby.engine

import apoy2k.robby.model.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals

class LaserTest {

    @ParameterizedTest
    @MethodSource("testDataSource")
    fun testFirstFieldByDirection(testData: TestData) {
        val endField = board.firstFieldByDirection(testData.startField, testData.direction, FieldType.WALL)
        assertEquals(testData.excpectedEndField, endField)
    }

    companion object {
        @JvmStatic
        val board = Board(listOf(
            listOf(Field(), Field(), Field()),
            listOf(Field(FieldType.WALL), Field(), Field()),
            listOf(Field(), Field(), Field(FieldType.WALL)),
            listOf(Field(), Field(FieldType.WALL), Field()),
        ))

        @JvmStatic
        fun testDataSource() = listOf(
            TestData(board.fields[0][0], Direction.RIGHT, board.fields[0][2]),
            TestData(board.fields[1][1], Direction.UP, board.fields[0][1]),
            TestData(board.fields[1][1], Direction.LEFT, board.fields[1][0]),
            TestData(board.fields[0][2], Direction.DOWN, board.fields[2][2]),
            TestData(board.fields[3][2], Direction.LEFT, board.fields[3][1]),
            TestData(board.fields[3][0], Direction.RIGHT, board.fields[3][1]),
            TestData(board.fields[3][1], Direction.UP, board.fields[0][1]),
            TestData(board.fields[0][0], Direction.LEFT, board.fields[0][0]),
            TestData(board.fields[0][0], Direction.UP, board.fields[0][0]),
            TestData(board.fields[2][2], Direction.RIGHT, board.fields[2][2]),
            TestData(board.fields[3][1], Direction.DOWN, board.fields[3][1]),
        )
    }
}

data class TestData(val startField: Field, val direction: Direction, val excpectedEndField: Field)
