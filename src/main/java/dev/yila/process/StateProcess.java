package dev.yila.process;

import dev.yila.functional.DirectResult;
import dev.yila.functional.Result;
import dev.yila.functional.failure.Failure;

import java.util.function.Function;

public class StateProcess<T> {
    public static <T> StateProcess<T> create(T initialValue) {
        var running = RunningStateProcess.start(initialValue);
        return new StateProcess<>(running);
    }

    private final RunningStateProcess<T> runningProcess;

    private StateProcess(RunningStateProcess<T> runningProcess) {
        this.runningProcess = runningProcess;
    }

    public <F extends Failure> Result<T, F> apply(Function<T, T> function) {
        return this.runningProcess.addFunction(function);
    }

    public <F extends Failure> Result<T, F> value() {
        return DirectResult.ok(runningProcess.value());
    }

    public Result<T, ? extends Failure> stop() {
        runningProcess.stop();
        return DirectResult.ok(runningProcess.value());
    }
}
