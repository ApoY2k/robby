package apoy2k.robby.model

import org.ktorm.database.Database
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
        return data.map { value -> enumClass.enumConstants.first { it.name == value } }
    }
}

inline fun <reified T : Enum<T>> BaseTable<*>.enumList(name: String): Column<Collection<T>> =
    registerColumn(name, EnumListType(T::class.java))

val Database.games get() = this.sequenceOf(Games)

val Database.fields get() = this.sequenceOf(Fields)

val Database.cards get() = this.sequenceOf(MovementCards)

val Database.robots get() = this.sequenceOf(Robots)
