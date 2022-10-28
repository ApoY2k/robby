package apoy2k.robby.kotlin

import apoy2k.robby.model.Fields
import apoy2k.robby.model.Games
import apoy2k.robby.model.MovementCards
import apoy2k.robby.model.Robots
import org.junit.jupiter.api.AfterEach
import org.ktorm.database.Database
import org.ktorm.dsl.deleteAll
import org.ktorm.logging.Slf4jLoggerAdapter
import org.ktorm.support.sqlite.SQLiteDialect
import org.slf4j.LoggerFactory

open class DatabaseBackedTest {
    val database = Database.connect(
        url = "jdbc:sqlite::memory",
        dialect = SQLiteDialect(),
        logger = Slf4jLoggerAdapter(LoggerFactory.getLogger("db")),
    )

    @AfterEach
    fun cleanup() {
        database.deleteAll(MovementCards)
        database.deleteAll(Robots)
        database.deleteAll(Fields)
        database.deleteAll(Games)
    }
}
