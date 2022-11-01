package apoy2k.robby.model

import org.ktorm.database.Database
import org.ktorm.entity.add
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.BaseTable
import org.ktorm.schema.Column
import org.ktorm.schema.SqlType
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

class EnumListType<T : Enum<T>>(
    private val enumClass: Class<T>
) : SqlType<Collection<T>>(Types.LONGVARCHAR, "enumList") {

    override fun doSetParameter(ps: PreparedStatement, index: Int, parameter: Collection<T>) {
        ps.setString(index, parameter.joinToString(","))
    }

    override fun doGetResult(rs: ResultSet, index: Int): Collection<T> {
        val data = rs.getString(index).orEmpty().split(",")
        return data
            .filter { it.isNotBlank() }
            .map { value -> enumClass.enumConstants.first { it.name == value } }
    }
}

inline fun <reified T : Enum<T>> BaseTable<*>.enumList(name: String): Column<Collection<T>> =
    registerColumn(name, EnumListType(T::class.java))

val Database.games get() = this.sequenceOf(Games)
val Database.fields get() = this.sequenceOf(Fields)
val Database.cards get() = this.sequenceOf(MovementCards)
val Database.robots get() = this.sequenceOf(Robots)
val Database.sessions get() = this.sequenceOf(Sessions)
val Database.users get() = this.sequenceOf(Users)

/**
 * Use the given matrix of fields and assign them to the given game, saving them in the database
 * with their corresponding coordinates
 */
fun Database.createFieldsForGame(fields: List<List<Field>>, gameId: Int) {
    val dbFields = fields
        .mapIndexed { row, rowFields ->
            rowFields.mapIndexed { col, field ->
                field.also {
                    it.gameId = gameId
                    it.positionX = col
                    it.positionY = row
                }
            }
        }
        .flatten()

    dbFields.forEach { this.fields.add(it) }
}
