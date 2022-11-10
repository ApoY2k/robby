package apoy2k.robby.model

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.assertEquals

class FieldTest {

    @ParameterizedTest
    @MethodSource("provideBlocksLaser")
    fun `blocks laser directions`(
        field: Field,
        direction: Direction,
        entry: Boolean,
        expectBlock: Boolean
    ) {
        val result = when (direction) {
            Direction.LEFT, Direction.RIGHT -> when (entry) {
                true -> field.blocksHorizontalLaserEntry(direction)
                false -> field.blocksHorizontalLaserExit(direction)
            }

            Direction.UP, Direction.DOWN -> when (entry) {
                true -> field.blocksVerticalLaserEntry(direction)
                false -> field.blocksVerticalLaserExit(direction)
            }
        }

        assertEquals(expectBlock, result)
    }

    companion object {

        @JvmStatic
        fun provideBlocksLaser(): Stream<Arguments> = Stream.of(
            Arguments.of(Field.new(), Direction.LEFT, false, false),
            Arguments.of(Field.new(), Direction.RIGHT, false, false),
            Arguments.of(Field.new(FieldElement.WALL, Direction.DOWN), Direction.UP, true, false),
            Arguments.of(Field.new(FieldElement.WALL, Direction.DOWN), Direction.DOWN, false, false),
            Arguments.of(Field.new(FieldElement.WALL, Direction.RIGHT, Direction.UP), Direction.LEFT, true, false),
            Arguments.of(Field.new(FieldElement.WALL, Direction.RIGHT, Direction.UP), Direction.UP, true, true),
            Arguments.of(Field.new(FieldElement.LASER_2, Direction.LEFT), Direction.RIGHT, false, true),
        )
    }
}
