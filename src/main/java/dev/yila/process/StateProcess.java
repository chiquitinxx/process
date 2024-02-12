package dev.yila.process;

import dev.yila.functional.DirectResult;
import dev.yila.functional.LazyResult;
import dev.yila.functional.Result;
import dev.yila.functional.failure.Failure;

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

    public <F extends Failure> Result<T, F> apply(Function<T, T> function) {
        var future = new CompletableFuture<T>();
        this.runningProcess.send(() -> {
            value = function.apply(value);
            future.complete(value);
        }, future::completeExceptionally);
        return LazyResult.create(future::join);
    }

    public <F extends Failure> Result<T, F> value() {
        return DirectResult.ok(value);
    }

    public Result<T, ? extends Failure> stop() {
        runningProcess.stop();
        return DirectResult.ok(value);
    }
}
