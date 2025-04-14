package dev.yila.process;

import dev.yila.functional.Pair;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

class RunningProcess implements Runnable {

    protected static RunningProcess start() {
        var running = new RunningProcess();
        Thread.ofVirtual().start(running);
        return running;
    }

    private boolean running = true;
    private final Queue<Pair<CheckedRunnable, Consumer>> queue;

    private RunningProcess() {
        this.queue = new LinkedBlockingQueue<>();
    }

    @Override
    public void run() {
        while (this.running) {
            if (!this.queue.isEmpty()) {
                var pair = this.queue.remove();
                try {
                    pair.getLeft().run();
                } catch (Throwable t) {
                    pair.getRight().accept(t);
                }
            }
        }
    }

    protected <T extends Throwable> void send(CheckedRunnable<T> runnable, Consumer<T> onException) {
        this.queue.add(Pair.of(runnable, onException));
    }

    protected void stop() {
        this.running = false;
    }
}
