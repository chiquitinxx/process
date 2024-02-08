package dev.yila.process;

import dev.yila.functional.Pair;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.function.Function;

class RunningProcess<T> implements Runnable {

    protected static <T> RunningProcess<T> start(T initialValue) {
        var running = new RunningProcess<>(initialValue);
        Thread.ofVirtual().start(running);
        return running;
    }

    private T value;
    private boolean running = true;
    private final BlockingQueue<Pair<Function<T, T>, Consumer<T>>> queue;

    private RunningProcess(T value) {
        this.value = value;
        this.queue = new LinkedBlockingQueue<>();
    }

    @Override
    public void run() {
        while (running) {
            if (!queue.isEmpty()) {
                var pair = queue.remove();
                this.value = pair.getLeft().apply(this.value);
                pair.getRight().accept(this.value);
            }
        }
    }

    protected void addFunction(Function<T, T> function, Consumer<T> callback) {
        this.queue.add(new Pair<>(function, callback));
    }

    protected T value() {
        return value;
    }

    protected void stop() {
        running = false;
    }
}
