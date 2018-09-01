package com.ifood.simplecircuitbreaker;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class SimpleConversionCircuitBreaker<@Nullable T> extends SimpleConversionCircuitBreakerBase<T> {
    public SimpleConversionCircuitBreaker(double minConversion, int minRequests, double recoveryRate) {
        this.minRequests = minRequests;
        this.minConversion = minConversion;
        this.recoveryRate = recoveryRate;
        checkInput();
    }

    public SimpleConversionCircuitBreaker(double minConversion) {
        this(minConversion, 1000, 0.1);
    }

    @NotNull
    public Wrapper wrap() {
        return new Wrapper();
    }

    public class Wrapper {
        Wrapper(){}

        private Predicate<T> successChecker;
        private Supplier<T> fallbackSupplier;

        @NotNull
        public Wrapper successChecker(@NotNull Predicate<T> successChecker) {
            this.successChecker = successChecker;
            return this;
        }

        @NotNull
        public Wrapper fallbackSupplier(@NotNull Supplier<T> fallbackSupplier) {
            this.fallbackSupplier = fallbackSupplier;
            return this;
        }

        public T execute(@NotNull Supplier<T> block) {
            return checkCircuitAndRun(this, block);
        }
    }

    private T checkCircuitAndRun(Wrapper wrapper, Supplier<T> block) {
        requireNonNull(block, "block");
        requireNonNull(wrapper.fallbackSupplier, "fallbackSupplier");
        requireNonNull(wrapper.successChecker, "successChecker");

        if (shouldReturnFallback()) {
            return wrapper.fallbackSupplier.get();
        }

        T result = block.get();
        boolean success = wrapper.successChecker.test(result);

        openOrCloseCircuit(success);

        return result;
    }

}
