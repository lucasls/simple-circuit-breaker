package com.ifood.simplecircuitbreaker.kotlin

import com.ifood.simplecircuitbreaker.SimpleConversionCircuitBreaker as JSimpleConversionCircuitBreaker

private fun getNotSupported(): Nothing {
    throw UnsupportedOperationException("Get is not supported")
}

class SimpleConversionCircuitBreaker<T>(
    minConversion: Double,
    minRequests: Int = 1000,
    recoveryRate: Double = 0.1
) {

    private val impl = JSimpleConversionCircuitBreaker<T>(minConversion, minRequests, recoveryRate)

    fun wrap(block: Wrapper.() -> Unit): Wrapper {
        return Wrapper().apply(block)
    }

    inner class Wrapper internal constructor() {
        private val wrapperImpl = impl.wrap()

        var fallbackSupplier: () -> T
            get() = getNotSupported()
            set(value) {
                wrapperImpl.fallbackSupplier(value)
            }

        var successChecker: (T) -> Boolean
            get() = getNotSupported()
            set(value) {
                wrapperImpl.successChecker(value)
            }

        fun execute(block: () -> T): T {
            return wrapperImpl.execute(block)
        }
    }

}