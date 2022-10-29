package apoy2k.robby.kotlin

import apoy2k.robby.setup
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class ApplicationTest : DatabaseBackedTest() {

    @Test
    fun `request to root`() = testApplication {
        val clock = Clock.fixed(Instant.parse("2022-01-01T00:00:00Z"), ZoneId.of("UTC"))

        application {
            setup(clock, database)
        }

        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }
}
