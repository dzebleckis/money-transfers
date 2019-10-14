package lt.dzebleckis.application

import java.util.concurrent.ArrayBlockingQueue

class BlockingCommandQueue : CommandQueue {

    private val queue = ArrayBlockingQueue<Command>(20)

    override fun get(): Command {
        return queue.take()
    }

    override fun add(command: Command): AsyncResult<String?> {
        queue.put(command)
        return command.result()
    }
}