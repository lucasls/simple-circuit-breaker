package com.ifood.simplecircuitbreaker;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

public class FutureSimpleConversionCircuitBreaker<@Nullable T> extends SimpleConversionCircuitBreakerBase<T> {
    public FutureSimpleConversionCircuitBreaker(double minConversion, int minRequests, double recoveryRate) {
        this.minRequests = minRequests;
        this.minConversion = minConversion;
        this.recoveryRate = recoveryRate;
        checkInput();
    }

    public FutureSimpleConversionCircuitBreaker(double minConversion) {
        this(minConversion, 1000, 0.1);
    }

    @NotNull
    public Wrapper wrap() {
        return new Wrapper();
    }

    public class Wrapper {
        Wrapper() {
        }

        private Function<T, CompletableFuture<Boolean>> successChecker;
        private Supplier<CompletableFuture<T>> fallbackSupplier;

        @NotNull
        public Wrapper successChecker(@NotNull Function<T, CompletableFuture<Boolean>> successChecker) {
            this.successChecker = successChecker;
            return this;
        }

        @NotNull
        public Wrapper fallbackSupplier(@NotNull Supplier<CompletableFuture<T>> fallbackSupplier) {
            this.fallbackSupplier = fallbackSupplier;
            return this;
        }

        public CompletableFuture<T> execute(@NotNull Supplier<CompletableFuture<T>> block) {
            return checkCircuitAndRun(this, block);
        }
    }

    private CompletableFuture<T> checkCircuitAndRun(Wrapper wrapper, Supplier<CompletableFuture<T>> block) {
        requireNonNull(block, "block");
        requireNonNull(wrapper.fallbackSupplier, "fallbackSupplier");
        requireNonNull(wrapper.successChecker, "successChecker");

        if (shouldReturnFallback()) {
            return wrapper.fallbackSupplier.get();
        }

        CompletableFuture<T> futureResult = block.get();

        return futureResult.thenCompose(result -> {
            CompletableFuture<Boolean> futureSuccess = wrapper.successChecker.apply(result);

            return futureSuccess.thenApply(success -> {
                openOrCloseCircuit(success);
                return result;
            });
        });
    }

}
