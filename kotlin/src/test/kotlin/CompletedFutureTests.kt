import com.ifood.simplecircuitbreaker.FutureSimpleConversionCircuitBreaker
import com.ifood.simplecircuitbreaker.kotlin.SimpleConversionCircuitBreaker
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

fun main(args: Array<String>) {

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
                        .fallbackSupplier { CompletableFuture.completedFuture("FAILED_CIRCUIT_OPEN") }
                        .successChecker { r -> CompletableFuture.completedFuture(r == "SUCCESS") }
                        .execute {
                            if (Math.random() <= currentConversion()) {
                                CompletableFuture.completedFuture("SUCCESS")
                            } else {
                                CompletableFuture.completedFuture("FAILED")
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