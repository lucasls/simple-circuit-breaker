package com.ifood.simplecircuitbreaker;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Objects;

abstract class SimpleConversionCircuitBreakerBase<T> {
    // Input
    protected int minRequests;
    protected double minConversion;
    protected double recoveryRate;

    // Implementation
    private Deque<Boolean> deque = new LinkedList<>();
    private int dequeSize = 0;
    private int successCounter = 0;
    protected boolean isCircuitOpen = false;

    protected void checkInput() {
        if (minRequests < 1) {
            throw new IllegalArgumentException("minRequests must be >= 1");
        }
        if (minConversion < 0 || minConversion > 1) {
            throw new IllegalArgumentException("minConversion must be between 0 and 1");
        }

        if (recoveryRate <= 0 || recoveryRate >= 1) {
            throw new IllegalArgumentException("recoveryRate must be between 0 and 1");
        }
    }

    protected void requireNonNull(Object any, String name) {
        Objects.requireNonNull(any, name + " argument must be supplied");
    }

    protected boolean shouldReturnFallback() {
        return isCircuitOpen && Math.random() > recoveryRate;
    }

    protected synchronized void openOrCloseCircuit(boolean success) {
        deque.addLast(success);
        if (success) {
            successCounter++;
        }

        int currentSize = dequeSize;
        dequeSize++;

        if (currentSize == minRequests) {
            boolean first = deque.removeFirst();

            if (first) {
                successCounter--;
            }

            dequeSize--;

            isCircuitOpen = successCounter / (double) minRequests < minConversion;
        }

        if (currentSize > minRequests) {
            throw new IllegalStateException("Queue size must never be higher than the minimum number of requests");
        }
    }
}
