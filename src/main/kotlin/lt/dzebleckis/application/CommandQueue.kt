package lt.dzebleckis.application

import java.util.concurrent.CompletableFuture

typealias AsyncResult<T> = CompletableFuture<T>

interface CommandQueue {
    fun get(): Command
    fun add(command: Command): AsyncResult<String?>
}
