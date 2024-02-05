package dev.yila.process;

import dev.yila.functional.AsyncResult;
import dev.yila.functional.DirectResult;
import dev.yila.functional.Result;
import dev.yila.functional.failure.Failure;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class Process<T> {
    public static <T> Process<T> create(T initialValue) {
        var running = RunningProcess.start(initialValue);
        return new Process<>(running);
    }

    private final RunningProcess<T> runningProcess;

    private Process(RunningProcess<T> runningProcess) {
        this.runningProcess = runningProcess;
    }

    protected RunningProcess<T> running() {
        return this.runningProcess;
    }

    public <F extends Failure> Result<T, F> send(Function<T, T> function) {
        var future = new CompletableFuture<T>();
        this.running().addFunction(function, future::complete);
        return AsyncResult.create(future);
    }

    public <F extends Failure> Result<T, F> value() {
        return DirectResult.ok(running().value());
    }
}
