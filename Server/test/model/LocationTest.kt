package apoy2k.robby.model

import apoy2k.robby.routes.Location
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.assertEquals

class LocationTest {

    @ParameterizedTest
    @MethodSource("provideTestReplaceParams")
    fun testReplaceParams(location: Location, params: Map<String, String>, expected: String) {
        assertEquals(expected, location.build(params))
    }

    companion object {
        @JvmStatic
        fun provideTestReplaceParams(): Stream<Arguments> = Stream.of(
            Arguments.of(
                Location.VIEW_GAME,
                mapOf("id" to "123"),
                "/game/123"
            )
        )
    }
}
