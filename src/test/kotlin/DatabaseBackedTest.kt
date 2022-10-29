package apoy2k.robby.kotlin

import apoy2k.robby.model.Fields
import apoy2k.robby.model.Games
import apoy2k.robby.model.MovementCards
import apoy2k.robby.model.Robots
import org.junit.jupiter.api.BeforeEach
import org.ktorm.database.Database
import org.ktorm.dsl.deleteAll
import org.ktorm.logging.Slf4jLoggerAdapter
import org.ktorm.support.sqlite.SQLiteDialect
import org.slf4j.LoggerFactory
import java.io.File

abstract class DatabaseBackedTest {
    val database = Database.connect(
        url = "jdbc:sqlite:tests.db",
        dialect = SQLiteDialect(),
        logger = Slf4jLoggerAdapter(LoggerFactory.getLogger("db")),
    )

    private val schema = File("database_schema.sql")
        .readText()
        .split(";")
        .filter { it.isNotBlank() }

    init {
        database.useConnection { connection ->
            schema.forEach { statement ->
                connection.createStatement().use {
                    it.execute(statement)
                }
            }
        }
    }

    @BeforeEach
    fun clearDatabase() {
        database.deleteAll(MovementCards)
        database.deleteAll(Robots)
        database.deleteAll(Fields)
        database.deleteAll(Games)
    }
}
