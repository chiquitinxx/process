package dev.yila.process;

import dev.yila.functional.AsyncResult;
import dev.yila.functional.Pair;
import dev.yila.functional.Result;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Experiment {

    static Map<ProcessId, AThread> threads = new ConcurrentHashMap<>();

    public static ProcessId create() {
        return create(Set.of());
    }

    public static boolean isAlive(ProcessId processId) {
        return threads.get(processId).isAlive();
    }

    public static ProcessId create(Set<OnMessage> processors) {
        var processId = generateNewProcessId();
        var thread = new AThread(processors);
        threads.put(processId, thread);
        return processId;
    }

    public static Result send(ProcessId processId, String messageName, Object message) {
        var thread = threads.get(processId);
        return thread.send(new Message(messageName, message));
    }

    private static ProcessId generateNewProcessId() {
        return new ProcessId(UUID.randomUUID().toString());
    }

    public static void stop(ProcessId processId) {
        threads.get(processId).stop();
        threads.remove(processId);
    }

    record Message(String name, Object message) {}

    static class AThread implements Runnable {

        private final Map<String, OnMessage> messageActions;
        private final Queue<Pair<Message, CompletableFuture<?>>> queue;
        private final Thread thread;
        private boolean running = true;
        private Consumer<Throwable> onThrowable = (_) -> {
            this.running = false;
        };

        AThread(Set<OnMessage> processors) {
            this.messageActions = processors.stream()
                    .collect(Collectors.toMap(OnMessage::name, o -> o));
            this.queue = new LinkedBlockingQueue<>();
            this.thread = Thread.ofVirtual().uncaughtExceptionHandler((thread, throwable) -> {
                onThrowable.accept(throwable);
            }).start(this);
        }

        Result<?> send(Message message) {
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
                    Message message = pair.getLeft();
                    Function function = messageActions.get(message.name()).function();
                    CompletableFuture cf = pair.getRight();
                    cf.complete(function.apply(message.message()));
                }
            }
            if (this.thread != null) {
                this.thread.interrupt();
            }
        }

        public void stop() {
            this.running = false;
        }
    }
}
