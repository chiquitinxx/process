package dev.yila.process;

import dev.yila.functional.Pair;
import org.junit.jupiter.api.Test;

import java.util.Set;

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
    public void senNumberMessage() {
        var add = new OnMessage("add", (Pair<Integer, Integer> pair) -> Integer.sum(pair.getLeft(), pair.getRight()));
        var processId = Experiment.create(Set.of(add));
        var response = Experiment.send(processId, "add", Pair.of(5, 8));

        assertEquals(13, response.getOrThrow());
    }
}
