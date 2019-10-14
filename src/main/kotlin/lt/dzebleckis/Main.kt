package lt.dzebleckis

import lt.dzebleckis.application.Application

fun main(args: Array<String>) {
    val app = Application(8090)

    Runtime.getRuntime().addShutdownHook(Thread {
        app.stop()
    })

    app.join()
}