package dev.yila.process;

public class Process {
    public static Process create() {
        var running = RunningProcess.start();
        return new Process(running);
    }

    private final RunningProcess runningProcess;

    private Process(RunningProcess runningProcess) {
        this.runningProcess = runningProcess;
    }

    public void stop() {
        runningProcess.stop();
    }
}
