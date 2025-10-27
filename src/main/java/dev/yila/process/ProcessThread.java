package dev.yila.process;

import dev.yila.functional.AsyncResult;
import dev.yila.functional.Pair;
import dev.yila.functional.Result;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProcessThread implements Runnable {

    private final Map<String, OnMessage> messageActions;
    private final Queue<Pair<Process.Message, CompletableFuture<?>>> queue;
    private final Thread thread;
    private boolean running = true;
    private Consumer<Throwable> onThrowable = (_) -> {
        this.running = false;
    };

    ProcessThread(Set<OnMessage> processors) {
        this.messageActions = processors.stream()
                .collect(Collectors.toMap(OnMessage::name, o -> o));
        this.queue = new LinkedBlockingQueue<>();
        this.thread = Thread.ofVirtual().uncaughtExceptionHandler((thread, throwable) -> {
            onThrowable.accept(throwable);
        }).start(this);
    }

    synchronized Result<?> send(Process.Message message) {
        var cf = new CompletableFuture<>();
        queue.add(new Pair<>(message, cf));
        return AsyncResult.create(cf);
    }

    boolean isAlive() {
        return thread.isAlive();
    }

    @Override
    public void run() {
        while (this.running) {
            if (!this.queue.isEmpty()) {
                var pair = this.queue.remove();
                Process.Message message = pair.getLeft();
                Function function = messageActions.get(message.name()).function();
                CompletableFuture cf = pair.getRight();
                try {
                    var result = function.apply(message.message());
                    cf.complete(result);
                } catch (Throwable t) {
                    cf.completeExceptionally(t);
                }
            }
        }
        if (this.thread != null) {
            this.thread.interrupt();
        }
    }

    void stop() {
        this.running = false;
    }
}
