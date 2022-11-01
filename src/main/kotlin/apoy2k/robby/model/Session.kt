package apoy2k.robby.model

import io.ktor.server.sessions.*
import org.ktorm.database.Database
import org.ktorm.dsl.delete
import org.ktorm.dsl.eq
import org.ktorm.dsl.insert
import org.ktorm.dsl.update
import org.ktorm.entity.Entity
import org.ktorm.entity.find
import org.ktorm.schema.Table
import org.ktorm.schema.varchar

data class Session(
    val userId: Int? = null,
) {
    val isLoggedIn = userId != null
}

object Sessions : Table<SessionData>("sessions") {
    var id = varchar("id").primaryKey().bindTo { it.id }
    var data = varchar("data").bindTo { it.data }
}

interface SessionData : Entity<SessionData> {
    var id: String
    var data: String
}

class DbSessionStorage(
    private val database: Database
) : SessionStorage {
    override suspend fun invalidate(id: String) {
        database.delete(Sessions) { it.id eq id }
    }

    override suspend fun read(id: String): String {
        return database.sessions.find { it.id eq id }?.data
            ?: throw NoSuchElementException("Session $id not found")
    }

    override suspend fun write(id: String, value: String) {
        val updated = database.update(Sessions) {
            set(it.data, value)
            where { it.id eq id }
        }
        if (updated == 0) {
            database.insert(Sessions) {
                set(it.id, id)
                set(it.data, value)
            }
        }
    }
}
