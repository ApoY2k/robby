package apoy2k.robby.kotlin.apo2k.robby

import apoy2k.robby.engine.BoardType
import apoy2k.robby.engine.GameEngine
import apoy2k.robby.engine.RobotEngine
import apoy2k.robby.engine.ViewUpdate
import apoy2k.robby.model.Action
import apoy2k.robby.model.RobotModel
import apoy2k.robby.model.Session
import apoy2k.robby.model.robots
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.find
import org.ktorm.logging.Slf4jLoggerAdapter
import org.ktorm.support.sqlite.SQLiteDialect
import org.slf4j.LoggerFactory
import java.time.Clock
import java.util.concurrent.Executors
import kotlin.test.fail

fun main() {
    val clock = Clock.systemDefaultZone()
    val database = Database.connect(
        url = "jdbc:sqlite::memory",
        dialect = SQLiteDialect(),
        logger = Slf4jLoggerAdapter(LoggerFactory.getLogger("db")),
    )

    val actionChannel = MutableSharedFlow<Action>()
    val viewUpdateChannel = MutableSharedFlow<ViewUpdate>()
    val robotEngine = RobotEngine(database)
    val gameEngine = GameEngine(clock, database, robotEngine, viewUpdateChannel)

    val s1 = Session(1)
    val s2 = Session(2)

    val game1 = gameEngine.createNewGame(BoardType.SANDBOX, 8)
    val game2 = gameEngine.createNewGame(BoardType.SANDBOX, 8)

    val pool = Executors.newCachedThreadPool().asCoroutineDispatcher()
    runBlocking(pool) {
        launch {
            gameEngine.connect(actionChannel)
        }

        launch {
            viewUpdateChannel.onEach { viewUpdate ->
                println("Received $viewUpdate")
            }.launchIn(this)
        }

        launch {
            actionChannel.emit(Action.joinGame(RobotModel.ZIPPY).also {
                it.game = game1
                it.session = s1
            })
            actionChannel.emit(Action.joinGame(RobotModel.HUZZA).also {
                it.game = game2
                it.session = s1
            })
        }

        // Leave some time for the engine to process
        delay(1000)

        val robot1 = database.robots.find { it.gameId eq game1.id and (it.userId eq (s1.userId ?: -1)) }
            ?: fail("Robot1 not found")
        val robot2 = database.robots.find { it.gameId eq game1.id and (it.userId eq (s2.userId ?: -1)) }
            ?: fail("Robot2 not found")

        val game1cards = robotEngine.getDrawnCards(robot1.id)
        val game2cards = robotEngine.getDrawnCards(robot2.id)

        launch {
            game1cards.take(5).forEachIndexed { idx, card ->
                actionChannel.emit(Action.selectCard(idx + 1, card.id).also {
                    it.game = game1
                    it.session = s1
                })
            }
            game2cards.take(5).forEachIndexed { idx, card ->
                actionChannel.emit(Action.selectCard(idx + 1, card.id).also {
                    it.game = game2
                    it.session = s1
                })
            }
        }

        delay(1000)
        launch {
            actionChannel.emit(Action.toggleReady().also {
                it.game = game1
                it.session = s1
            })
            actionChannel.emit(Action.toggleReady().also {
                it.game = game2
                it.session = s1
            })
        }
    }
}
