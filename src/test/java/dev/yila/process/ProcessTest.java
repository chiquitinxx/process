package dev.yila.process;

import dev.yila.functional.Pair;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

public class ProcessTest {

    @Test
    public void create() {
        var processId = Process.create();
        assertNotNull(processId);
        assertTrue(Process.isAlive(processId));
    }

    @Test
    public void sendTextMessage() {
        var onHello = new OnMessage("hello", message -> "hello " + message);
        var processId = Process.create(Set.of(onHello));
        var response = Process.send(processId, "hello", "world");

        assertEquals("hello world", response.getOrThrow());
    }

    @Test
    public void sendNumberMessage() {
        var add = new OnMessage("add", (Pair<Integer, Integer> pair) -> Integer.sum(pair.getLeft(), pair.getRight()));
        var processId = Process.create(Set.of(add));
        var response = Process.send(processId, "add", Pair.of(5, 8));

        assertEquals(13, response.getOrThrow());
    }

    @Test
    public void canStartMillion() {
        var stack = new Stack<ProcessId>();
        for (int i = 0; i < 1_000_000; i++) {
            var processId = Process.create(Set.of());
            stack.push(processId);
        }
        while (!stack.isEmpty()) {
            Process.stop(stack.pop());
        }
    }

    @Test
    public void sendOneMillionMessages() {
        var atomic = new AtomicInteger(0);
        var inc = new OnMessage("inc", (_) -> atomic.incrementAndGet());
        var processId = Process.create(Set.of(inc));
        for (int i = 0; i < 1_000_000; i++) {
            Process.send(processId, "inc", null);
        }
        await().until(() -> 1_000_000 == atomic.get());
    }

    @Test
    public void exceptionReadingMessage() {
        var error = new OnMessage("error", (_) -> {
            throw new RuntimeException();
        });
        var processId = Process.create(Set.of(error));
        var result = Process.send(processId, "error", null);

        assertTrue(result.hasFailure());
    }
}
