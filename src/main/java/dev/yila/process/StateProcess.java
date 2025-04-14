package dev.yila.process;

import dev.yila.functional.DirectResult;
import dev.yila.functional.LazyResult;
import dev.yila.functional.Result;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class StateProcess<T> {
    public static <T> StateProcess<T> create(T initialValue) {
        var running = RunningProcess.start();
        return new StateProcess<>(running, initialValue);
    }

    private T value;
    private final RunningProcess runningProcess;

    private StateProcess(RunningProcess runningProcess, T initialValue) {
        this.runningProcess = runningProcess;
        this.value = initialValue;
    }

    public Result<T> apply(Function<T, T> function) {
        var future = new CompletableFuture<T>();
        this.runningProcess.send(() -> {
            value = function.apply(value);
            future.complete(value);
        }, future::completeExceptionally);
        return LazyResult.create(future::join);
    }

    public Result<T> value() {
        return DirectResult.ok(value);
    }

    public Result<T> stop() {
        runningProcess.stop();
        return DirectResult.ok(value);
    }
}
