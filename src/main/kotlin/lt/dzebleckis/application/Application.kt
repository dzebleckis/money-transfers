package lt.dzebleckis.application

import lt.dzebleckis.api.rest.RestServer
import lt.dzebleckis.infrastructure.InMemoryHolderRepository
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

class Application(port: Int) {

    private var server: RestServer by Delegates.notNull()
    private var executor: ExecutorService by Delegates.notNull()
    private var queue: BlockingCommandQueue by Delegates.notNull()

    init {
        val holderRepository = InMemoryHolderRepository()

        queue = BlockingCommandQueue()
        executor = Executors.newFixedThreadPool(1)
        executor.submit {
            CommandProcessor(queue, lt.dzebleckis.domain.HolderService(holderRepository))
        }

        server = RestServer(port, HolderService(holderRepository, queue))
        server.start()
    }

    fun join() {
        server.join()
    }

    fun stop() {
        server.stop()
        queue.add(PoisonPill)
        executor.shutdown()
        executor.awaitTermination(10, TimeUnit.SECONDS)
    }
}

