package apoy2k.robby.model

import apoy2k.robby.encryptPassword
import apoy2k.robby.generateSalt
import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar
import java.util.*

@Suppress("unused")
object Users : Table<User>("users") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
    val password = varchar("password").bindTo { it.password }
    val salt = varchar("salt").bindTo { it.salt }
}

interface User : Entity<User> {
    var id: Int
    var name: String
    var password: String
    var salt: String

    companion object : Entity.Factory<User>() {
        @JvmStatic
        fun new(name: String, password: String): User {
            val salt = generateSalt()
            return User {
                this.name = name
                this.salt = Base64.getEncoder().encodeToString(salt)
                this.password = Base64.getEncoder().encodeToString(encryptPassword(password, salt))
            }
        }
    }
}
