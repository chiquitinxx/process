package dev.yila.process;

import dev.yila.functional.Result;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Process {

    static Map<ProcessId, ProcessThread> threads = new ConcurrentHashMap<>();

    public static ProcessId create() {
        return create(Set.of());
    }

    public static boolean isAlive(ProcessId processId) {
        return threads.get(processId).isAlive();
    }

    public static ProcessId create(Set<OnMessage> processors) {
        var processId = generateNewProcessId();
        var thread = new ProcessThread(processors);
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

    public static String MESSAGE_SET_STATE = "setState";

    record Message(String name, Object message) {}
}
