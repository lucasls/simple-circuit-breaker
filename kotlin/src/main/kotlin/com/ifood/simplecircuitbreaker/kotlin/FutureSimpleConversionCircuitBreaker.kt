package com.ifood.simplecircuitbreaker.kotlin

import java.util.concurrent.CompletableFuture
import com.ifood.simplecircuitbreaker.FutureSimpleConversionCircuitBreaker as JFutureSimpleConversionCircuitBreaker

private fun getNotSupported(): Nothing {
    throw UnsupportedOperationException("Get is not supported")
}

class FutureSimpleConversionCircuitBreaker<T>(
    minConversion: Double,
    minRequests: Int = 1000,
    recoveryRate: Double = 0.1
) {

    private val impl = JFutureSimpleConversionCircuitBreaker<T>(minConversion, minRequests, recoveryRate)

    fun wrap(block: Wrapper.() -> Unit): Wrapper {
        return Wrapper().apply(block)
    }

    inner class Wrapper internal constructor() {
        private val wrapperImpl = impl.wrap()

        var fallbackSupplier: () -> CompletableFuture<T>
            get() = getNotSupported()
            set(value) {
                wrapperImpl.fallbackSupplier(value)
            }

        var successChecker: (T) -> CompletableFuture<Boolean>
            get() = getNotSupported()
            set(value) {
                wrapperImpl.successChecker(value)
            }

        fun execute(block: () -> CompletableFuture<T>): CompletableFuture<T> {
            return wrapperImpl.execute(block)
        }
    }

}