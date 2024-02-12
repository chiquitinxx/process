package dev.yila.process;

import dev.yila.functional.LazyResult;
import dev.yila.functional.Pair;
import dev.yila.functional.Result;
import dev.yila.functional.failure.Failure;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;

class RunningStateProcess<T> implements Runnable {

    protected static <T> RunningStateProcess<T> start(T initialValue) {
        var running = new RunningStateProcess<>(initialValue);
        Thread.ofVirtual().start(running);
        return running;
    }

    private T value;
    private boolean running = true;
    private final Queue<Pair<Function<T, T>, CompletableFuture<T>>> queue;

    private RunningStateProcess(T value) {
        this.value = value;
        this.queue = new LinkedBlockingQueue<>();
    }

    @Override
    public void run() {
        while (this.running) {
            if (!this.queue.isEmpty()) {
                var pair = this.queue.remove();
                try {
                    this.value = pair.getLeft().apply(this.value);
                    pair.getRight().complete(this.value);
                } catch (RuntimeException runtimeException) {
                    pair.getRight().completeExceptionally(runtimeException);
                }
            }
        }
    }

    protected <F extends Failure> Result<T, F> addFunction(Function<T, T> function) {
        var future = new CompletableFuture<T>();
        this.queue.add(new Pair<>(function, future));
        return LazyResult.create(future::join);
    }

    protected T value() {
        return this.value;
    }

    protected void stop() {
        this.running = false;
    }
}