package tornadofx

import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.concurrent.Task

fun <T> List<T>.observable() = FXCollections.observableList(this)

fun <T> task(func: () -> T) = object : Task<T>() {
    override fun call(): T {
        return func()
    }
}.apply {
    setOnFailed({ Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), exception) })
    Thread(this).start()
}

infix fun <T> Task<T>.success(func: (T) -> Unit): Task<T> {
    Platform.runLater {
        setOnSucceeded { func(value) }
    }
    return this
}

fun runFXSync(action: () -> Unit) {
    // run synchronously on JavaFX thread
    if (Platform.isFxApplicationThread()) {
        action()
        return
    }

    // queue on JavaFX thread and wait for completion
    val doneLatch = CountDownLatch(1)
    Platform.runLater {
        try {
            action()
        } finally {
            doneLatch.countDown()
        }
    }

    try {
        doneLatch.await()
    } catch (e: InterruptedException) {
        // ignore exception
    }
}
