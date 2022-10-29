package apoy2k.robby.kotlin

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.ktorm.database.Database
import org.ktorm.logging.Slf4jLoggerAdapter
import org.ktorm.support.sqlite.SQLiteDialect
import org.slf4j.LoggerFactory
import java.io.File

abstract class DatabaseBackedTest {
    val database = Database.connect(
        url = "jdbc:sqlite::memory:",
        dialect = SQLiteDialect(),
        logger = Slf4jLoggerAdapter(LoggerFactory.getLogger("db")),
    )

    private val schema = File("database_schema.sql")
        .readText()
        .split(";")
        .filter { it.isNotBlank() }

    abstract fun setupBeforeEach()
    abstract fun tearDownAfterEach()

    @BeforeEach
    fun setup() {
        database.useConnection { connection ->
            schema.forEach { statement ->
                connection.prepareStatement(statement).execute()
            }
        }
        setupBeforeEach()
    }

    @AfterEach
    fun tearDown() {
        tearDownAfterEach()
        database.useConnection {
            it.prepareStatement("PRAGMA writable_schema = 1").execute()
            it.prepareStatement("DELETE FROM sqlite_master WHERE type IN ('table', 'index', 'trigger')").execute()
            it.prepareStatement("PRAGMA writable_schema = 0").execute()
            it.prepareStatement("VACUUM").execute()
            it.prepareStatement("PRAGMA INTEGRITY_CHECK").execute()
        }
    }
}
