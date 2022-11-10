package apoy2k.robby.model

import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.isNotNull
import org.ktorm.dsl.isNull
import org.ktorm.entity.*
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

private val Database.games get() = this.sequenceOf(Games)
private val Database.fields get() = this.sequenceOf(Fields)
private val Database.cards get() = this.sequenceOf(MovementCards)
private val Database.robots get() = this.sequenceOf(Robots)
private val Database.sessions get() = this.sequenceOf(Sessions)
private val Database.users get() = this.sequenceOf(Users)

fun Database.session(id: String) = sessions.find { it.id eq id }?.data

fun Database.games() = games.map { it }
fun Database.game(id: Int) = games.find { it.id eq id }
fun Database.update(game: Game) = games.update(game)
fun Database.add(game: Game) = games.add(game)

fun Database.fieldsFor(id: Int) = fields.filter { it.gameId eq id }.map { it }
fun Database.update(field: Field) = fields.update(field)
fun Database.add(field: Field) = fields.add(field)

fun Database.user(session: Session?) = users.find { it.id eq (session?.userId ?: -1) }
fun Database.user(name: String) = users.find { it.name eq name }
fun Database.add(user: User) = users.add(user)

fun Database.card(id: Int) = cards.find { it.id eq id }
fun Database.cardsForGame(gameId: Int) = cards.filter { it.gameId eq gameId }.map { it }
fun Database.cardsForRegister(gameId: Int, register: Int) = cards
    .filter { (it.gameId eq gameId) and (it.register eq register) }

fun Database.cardsWithoutRobot(gameId: Int) = cards
    .filter { it.gameId eq gameId and it.robotId.isNull() }
    .map { it }

fun Database.cardsForRobot(robotId: Int) = cards.filter { it.robotId eq robotId }.map { it }
fun Database.cardsForRobot(robotId: Int, register: Int) = cards
    .find { it.robotId eq robotId and (it.register eq register) }

fun Database.cardsForRobotNoRegister(robotId: Int) = cards
    .filter { it.robotId eq robotId and it.register.isNotNull() }

fun Database.cardCountForRobotRegister(robotId: Int) = cards
    .count { it.robotId eq robotId and it.register.isNotNull() }

fun Database.update(card: MovementCard) = cards.update(card)
fun Database.add(card: MovementCard) = cards.add(card)

fun Database.robots() = robots.map { it }
fun Database.robotFor(gameId: Int, userId: Int) = robots.find { it.gameId eq gameId and (it.userId eq userId) }
fun Database.robotsFor(gameId: Int) = robots.filter { it.gameId eq gameId }.map { it }
fun Database.robotCount(gameId: Int) = robots.count { it.gameId eq gameId }
fun Database.update(robot: Robot) = robots.update(robot)
fun Database.add(robot: Robot) = robots.add(robot)
fun Database.remove(robotId: Int) = robots.removeIf { it.id eq robotId }
