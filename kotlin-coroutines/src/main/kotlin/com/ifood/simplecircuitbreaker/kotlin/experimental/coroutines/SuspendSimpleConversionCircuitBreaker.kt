package com.ifood.simplecircuitbreaker.kotlin.experimental.coroutines

import com.ifood.simplecircuitbreaker.kotlin.FutureSimpleConversionCircuitBreaker
import kotlinx.coroutines.experimental.future.await
import kotlinx.coroutines.experimental.future.future

private fun getNotSupported(): Nothing {
    throw UnsupportedOperationException("Get is not supported")
}

// TODO replace CompletableFuture based implementation with pure coroutines impl
class SuspendSimpleConversionCircuitBreaker<T>(
    minConversion: Double,
    minRequests: Int = 1000,
    recoveryRate: Double = 0.1
) {
    private val impl = FutureSimpleConversionCircuitBreaker<T>(minConversion, minRequests, recoveryRate)

    fun wrap(block: Wrapper.() -> Unit): Wrapper {
        return Wrapper().apply(block)
    }

    inner class Wrapper internal constructor() {
        private val wrapperImpl = impl.wrap {  }

        var fallbackSupplier: suspend () -> T
            get() = getNotSupported()
            set(value) {
                wrapperImpl.fallbackSupplier = { future { value() } }
            }

        var successChecker: suspend (T) -> Boolean
            get() = getNotSupported()
            set(value) {
                wrapperImpl.successChecker = {t -> future { value(t) } }
            }

        suspend fun execute(block: suspend () -> T): T {
            return wrapperImpl.execute { future { block() } }.await()
        }
    }
}