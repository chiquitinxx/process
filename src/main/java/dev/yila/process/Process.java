package dev.yila.process;

import java.util.function.Consumer;

public class Process {
    public static Process create() {
        var running = RunningProcess.start();
        return new Process(running);
    }

    private final RunningProcess runningProcess;

    private Process(RunningProcess runningProcess) {
        this.runningProcess = runningProcess;
    }

    public void send(Runnable runnable) {
        this.send(runnable, re -> {
            //Nothing to do? just hide?
        });
    }

    public void send(Runnable runnable, Consumer<RuntimeException> consumer) {
        runningProcess.send(runnable, consumer);
    }

    public void stop() {
        runningProcess.stop();
    }
}
