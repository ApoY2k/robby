package apoy2k.robby.kotlin.model

import apoy2k.robby.model.Direction
import apoy2k.robby.model.Field
import apoy2k.robby.model.FieldElement
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
        isVertical: Boolean,
        expectBlock: Boolean
    ) {
        val result = when (isVertical) {
            V -> field.blocksVerticalLaser()
            H -> field.blocksHorizontalLaser()
            else -> throw Exception("Unknow laser direction")
        }
        assertEquals(expectBlock, result)
    }

    companion object {
        @JvmStatic
        val V = true

        @JvmStatic
        val H = false

        @JvmStatic
        fun provideBlocksLaser(): Stream<Arguments> = Stream.of(
            Arguments.of(Field.new(), H, false),
            Arguments.of(Field.new(), V, false),
            Arguments.of(Field.new(FieldElement.WALL, Direction.DOWN), V, true),
            Arguments.of(Field.new(FieldElement.WALL, Direction.DOWN), H, false),
            Arguments.of(Field.new(FieldElement.WALL, Direction.RIGHT, Direction.UP), V, true),
            Arguments.of(Field.new(FieldElement.WALL, Direction.RIGHT, Direction.UP), H, true),
            Arguments.of(Field.new(FieldElement.WALL, Direction.LEFT), V, false),
            Arguments.of(Field.new(FieldElement.LASER, Direction.LEFT), V, false),
            Arguments.of(Field.new(FieldElement.LASER, Direction.LEFT), H, true),
            Arguments.of(Field.new(FieldElement.LASER_2, Direction.UP), V, true),
            Arguments.of(Field.new(FieldElement.LASER_2, Direction.UP), H, false),
            Arguments.of(Field.new(FieldElement.PUSHER, Direction.LEFT), V, false),
            Arguments.of(Field.new(FieldElement.PUSHER, Direction.LEFT), H, true),
        )
    }
}
