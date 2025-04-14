package dev.yila.process;

import java.util.function.Consumer;

public class Process {

    private static final Consumer<RuntimeException> DEFAULT_RUNTIME_EXCEPTION_CONSUMER =
            Throwable::printStackTrace;

    public static Process create() {
        var running = RunningProcess.start();
        return new Process(running);
    }

    private final RunningProcess runningProcess;

    private Process(RunningProcess runningProcess) {
        this.runningProcess = runningProcess;
    }

    public void send(Runnable runnable) {
        this.send(CheckedRunnable.from(runnable), DEFAULT_RUNTIME_EXCEPTION_CONSUMER);
    }

    public <T extends Throwable> void send(CheckedRunnable<T> runnable, Consumer<T> consumer) {
        runningProcess.send(runnable, consumer);
    }

    public void stop() {
        runningProcess.stop();
    }
}
