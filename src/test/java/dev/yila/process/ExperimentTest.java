package dev.yila.process;

import dev.yila.functional.Pair;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

public class ExperimentTest {

    @Test
    public void create() {
        var processId = Experiment.create();
        assertNotNull(processId);
        assertTrue(Experiment.isAlive(processId));
    }

    @Test
    public void sendTextMessage() {
        var onHello = new OnMessage("hello", message -> "hello " + message);
        var processId = Experiment.create(Set.of(onHello));
        var response = Experiment.send(processId, "hello", "world");

        assertEquals("hello world", response.getOrThrow());
    }

    @Test
    public void sendNumberMessage() {
        var add = new OnMessage("add", (Pair<Integer, Integer> pair) -> Integer.sum(pair.getLeft(), pair.getRight()));
        var processId = Experiment.create(Set.of(add));
        var response = Experiment.send(processId, "add", Pair.of(5, 8));

        assertEquals(13, response.getOrThrow());
    }

    @Test
    public void canStartMillion() {
        var stack = new Stack<ProcessId>();
        for (int i = 0; i < 1_000_000; i++) {
            var processId = Experiment.create(Set.of());
            stack.push(processId);
        }
        while (!stack.isEmpty()) {
            Experiment.stop(stack.pop());
        }
    }

    @Test
    public void sendOneMillionMessages() {
        var atomic = new AtomicInteger(0);
        var inc = new OnMessage("inc", (_) -> atomic.incrementAndGet());
        var processId = Experiment.create(Set.of(inc));
        for (int i = 0; i < 1_000_000; i++) {
            Experiment.send(processId, "inc", null);
        }
        await().until(() -> 1_000_000 == atomic.get());
    }
}
