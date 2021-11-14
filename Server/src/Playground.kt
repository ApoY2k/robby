package apoy2k.robby

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.time.Instant
import java.util.concurrent.Executors
import kotlin.system.exitProcess

fun main() {
    val pool = Executors.newCachedThreadPool().asCoroutineDispatcher()
    val actionFlow = MutableSharedFlow<Unit>()
    val viewUpdateFlow = MutableSharedFlow<Unit>()

    runBlocking(pool) {
        launch {
            connect(actionFlow, viewUpdateFlow)
        }

        launch {
            simulateSendViewUpdatesOnWebsocket(viewUpdateFlow)
        }

        println("${Instant.now()} [${Thread.currentThread().name}] Sending action")
        actionFlow.emit(Unit)

        println("${Instant.now()} [${Thread.currentThread().name}] Sending action")
        actionFlow.emit(Unit)

        delay(10_000)
        exitProcess(0)
    }
}

suspend fun connect(actions: SharedFlow<Unit>, viewUpdates: MutableSharedFlow<Unit>) {
    coroutineScope {
        actions.onEach {
            println("${Instant.now()} [${Thread.currentThread().name}] Received action")
            launch {
                calc(viewUpdates)
            }
        }.launchIn(this)
    }
}

suspend fun calc(viewUpdates: MutableSharedFlow<Unit>) {
    println("${Instant.now()} [${Thread.currentThread().name}] Starting calculation")
    viewUpdates.emit(Unit)
    for (i in 1..10) {
        delay(100)
    }
    println("${Instant.now()} [${Thread.currentThread().name}] Finished calculation")
    viewUpdates.emit(Unit)
}

suspend fun simulateSendViewUpdatesOnWebsocket(flow: SharedFlow<Unit>) = coroutineScope {
    flow.onEach {
        println("${Instant.now()} [${Thread.currentThread().name}] Sending ViewUpdate")
    }.launchIn(this)
}
