package apoy2k.robby

import apoy2k.robby.engine.GameEngine
import apoy2k.robby.model.*
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors

fun main() {
    val game1 = Game()
    val game2 = Game()
    val s1 = Session("s1", "player1")

    val actions = MutableSharedFlow<Action>()
    val viewUpdates = MutableSharedFlow<ViewUpdate>()
    val engine = GameEngine(viewUpdates)

    val pool = Executors.newCachedThreadPool().asCoroutineDispatcher()
    runBlocking(pool) {
        launch {
            engine.connect(actions)
        }

        launch {
            viewUpdates.onEach { viewUpdate ->
                println("Received $viewUpdate")
            }.launchIn(this)
        }

        launch {
            actions.emit(JoinGameAction(RobotModel.ZIPPY.name).also {
                it.game = game1
                it.session = s1
            })
            actions.emit(JoinGameAction(RobotModel.HUZZA.name).also {
                it.game = game2
                it.session = s1
            })
        }

        // Leave some time for the engine to process
        delay(1000)

        val game1cards = game1.playerFor(s1)?.drawnCards.orEmpty()
        val game2cards = game2.playerFor(s1)?.drawnCards.orEmpty()

        launch {
            game1cards.take(5).forEachIndexed { idx, card ->
                actions.emit(SelectCardAction((idx + 1).toString(), card.id).also {
                    it.game = game1
                    it.session = s1
                })
            }
            game2cards.take(5).forEachIndexed { idx, card ->
                actions.emit(SelectCardAction((idx + 1).toString(), card.id).also {
                    it.game = game2
                    it.session = s1
                })
            }
        }

        delay(1000)
        launch {
            actions.emit(ConfirmCardsAction().also {
                it.game = game1
                it.session = s1
            })
            actions.emit(ConfirmCardsAction().also {
                it.game = game2
                it.session = s1
            })
        }
    }
}
