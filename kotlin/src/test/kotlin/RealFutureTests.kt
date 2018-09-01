import com.ifood.simplecircuitbreaker.FutureSimpleConversionCircuitBreaker
import com.ifood.simplecircuitbreaker.kotlin.SimpleConversionCircuitBreaker
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {

    val scheduler = Executors.newSingleThreadScheduledExecutor()

    fun <T> delay(t: Long, block: () -> T): CompletableFuture<T> {
        return CompletableFuture<T>().also { result ->
            scheduler.schedule({
                ForkJoinPool.commonPool().execute {
                    result.complete(block())
                }
            }, t, TimeUnit.MILLISECONDS)
        }
    }

    fun <T> delay(t: Long, value: T): CompletableFuture<T> {
        return delay(t) { value }
    }

    val circuitBreaker = FutureSimpleConversionCircuitBreaker<String>(
        0.3, 20, 0.1
    )

    val threadPool = Executors.newCachedThreadPool()

    var currentConversion = 100
    fun currentConversion() = currentConversion / 100.0

    repeat(5) {
        threadPool.submit {
            while (true) {
                try {
                    val result = circuitBreaker.wrap()
                        .fallbackSupplier { delay(300, "FAILED_CIRCUIT_OPEN") }
                        .successChecker { r -> CompletableFuture.completedFuture(r == "SUCCESS") }
                        .execute {
                            if (Math.random() <= currentConversion()) {
                                delay(200, "SUCCESS")
                            } else {
                                delay(600, "FAILED")
                            }
                        }

                    result.thenAccept { println(it) }

                } catch (e: Exception) {
                    e.printStackTrace()
                    throw e
                }

                Thread.sleep((Math.random() * 300).toLong())
            }
        }
    }

    while (currentConversion > 20) {
        Thread.sleep(1000)
        currentConversion -= 10
        println("*********************************************************************************")
        println("***************** DECREASING CONVERSION TO $currentConversion% ******************")
        println("*********************************************************************************")
    }


    println("\n\n************************************************")
    println("***************** WAITING 10s ******************")
    println("************************************************")
    Thread.sleep(10000)

    while (currentConversion < 100) {
        currentConversion += 10
        println("*********************************************************************************")
        println("***************** INCREASING CONVERSION TO $currentConversion% ******************")
        println("*********************************************************************************")
        Thread.sleep(1000)
    }
}