package apoy2k.robby.kotlin

import apoy2k.robby.setup
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.ktorm.database.Database
import org.ktorm.logging.Slf4jLoggerAdapter
import org.ktorm.support.sqlite.SQLiteDialect
import org.slf4j.LoggerFactory
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class ApplicationTest {

    @Test
    fun `request to root`() = testApplication {
        val clock = Clock.fixed(Instant.parse("2022-01-01T00:00:00Z"), ZoneId.of("UTC"))
        val database = Database.connect(
            url = "jdbc:sqlite::memory",
            dialect = SQLiteDialect(),
            logger = Slf4jLoggerAdapter(LoggerFactory.getLogger("db")),
        )

        application {
            setup(clock, database)
        }

        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }
}
