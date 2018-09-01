import com.ifood.simplecircuitbreaker.kotlin.experimental.coroutines.SuspendSimpleConversionCircuitBreaker
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import java.util.concurrent.Executors

fun main(args: Array<String>) = runBlocking {
    suspend fun <T> delay(t: Long, block: () -> T): T {
        delay(t)
        return block()
    }

    suspend fun <T> delay(t: Long, value: T): T {
        return delay(t) { value }
    }

    val circuitBreaker = SuspendSimpleConversionCircuitBreaker<String>(
        0.3, 20, 0.1
    )

    val threadPool = Executors.newCachedThreadPool()

    var currentConversion = 100
    fun currentConversion() = currentConversion / 100.0

    repeat(5) {
        async{
            while (true) {
                try {
                    val result = circuitBreaker.wrap {
                        fallbackSupplier = { delay(300, "FAILED_CIRCUIT_OPEN") }
                        successChecker = { r -> r == "SUCCESS" }
                    }.execute {
                            if (Math.random() <= currentConversion()) {
                                delay(200, "SUCCESS")
                            } else {
                                delay(600, "FAILED")
                            }
                        }

                    println(result)

                } catch (e: Exception) {
                    e.printStackTrace()
                    throw e
                }

                delay((Math.random() * 300).toLong())
            }
        }
    }

    while (currentConversion > 20) {
        delay(1000)
        currentConversion -= 10
        println("*********************************************************************************")
        println("***************** DECREASING CONVERSION TO $currentConversion% ******************")
        println("*********************************************************************************")
    }


    println("\n\n************************************************")
    println("***************** WAITING 10s ******************")
    println("************************************************")
    delay(10000)

    while (currentConversion < 100) {
        currentConversion += 10
        println("*********************************************************************************")
        println("***************** INCREASING CONVERSION TO $currentConversion% ******************")
        println("*********************************************************************************")
        delay(1000)
    }
}