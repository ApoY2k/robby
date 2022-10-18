package apoy2k.robby.kotlin

import apoy2k.robby.data.MemoryStorage
import apoy2k.robby.setup
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ApplicationTest {

    @Test
    fun `request to root`() = testApplication {
        val storage = MemoryStorage()

        application {
            setup(storage)
        }

        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }
}
