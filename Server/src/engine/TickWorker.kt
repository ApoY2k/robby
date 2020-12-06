package apoy2k.robby.engine

import apoy2k.robby.model.Action
import apoy2k.robby.model.ExecuteMovementAction
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors

class TickWorker(private val channel: Channel<Action>) {
    private val executorService = Executors.newFixedThreadPool(1)

    fun start() {
        executorService.submit {
            while (true) {
                runBlocking {
                    channel.send(ExecuteMovementAction())
                }
                Thread.sleep(500)
            }
        }
    }
}
