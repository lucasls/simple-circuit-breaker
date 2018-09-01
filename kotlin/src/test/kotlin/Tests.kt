import com.ifood.simplecircuitbreaker.kotlin.SimpleConversionCircuitBreaker
import java.util.concurrent.Executors

fun main(args: Array<String>) {

    val circuitBreaker = SimpleConversionCircuitBreaker<String>(
        minConversion = 0.3,
        minRequests = 20
    )

    val threadPool = Executors.newCachedThreadPool()

    var currentConversion = 100
    fun currentConversion() = currentConversion / 100.0

    repeat(5) {
        threadPool.submit {
            while (true) {
                try {
                    val result = circuitBreaker.wrap {
                        fallbackSupplier = { "FAILED_CIRCUIT_OPEN" }
                        successChecker = { it == "SUCCESS" }
                    }.execute {
                        if (Math.random() <= currentConversion()) {
                            "SUCCESS"
                        } else {
                            "FAILED"
                        }
                    }

                    println(result)
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