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
    private final Queue<Pair<Runnable, Consumer<RuntimeException>>> queue;

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
                } catch (RuntimeException runtimeException) {
                    pair.getRight().accept(runtimeException);
                }
            }
        }
    }

    protected void send(Runnable runnable, Consumer<RuntimeException> onException) {
        this.queue.add(Pair.of(runnable, onException));
    }

    protected void stop() {
        this.running = false;
    }
}
