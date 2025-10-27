package dev.yila.process;

import dev.yila.functional.DirectResult;
import dev.yila.functional.Result;

import java.util.Set;
import java.util.function.Function;

public class StateProcess<T> {
    public static <T> StateProcess<T> create(T initialValue) {
        return new StateProcess<>(initialValue);
    }

    private T value;
    private final ProcessThread runningProcess;

    private StateProcess(T initialValue) {
        this.value = initialValue;
        this.runningProcess = new ProcessThread(Set.of(
                new OnMessage(Process.MESSAGE_SET_STATE, input -> {
                        this.value = ((Function<T, T>) input).apply(value);
                        return value;
                })
        ));
    }

    public Result<T> apply(Function<T, T> function) {
        return (Result<T>) this.runningProcess.send(
                new Process.Message(Process.MESSAGE_SET_STATE, function));
    }

    public Result<T> value() {
        return DirectResult.ok(value);
    }

    public Result<T> stop() {
        runningProcess.stop();
        return DirectResult.ok(value);
    }
}
