package dev.yila.process;

import org.junit.jupiter.api.Test;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProcessTest {

    @Test
    void creation() {
        var process = Process.create(0);

        var result = process.send(prev -> prev + 1);

        assertEquals(1, result.getOrThrow());
    }

    @Test
    void sendMultipleMessages() {
        var process = Process.create(0);

        process.send(prev -> prev + 1000);
        process.send(prev -> prev + 100);
        process.send(prev -> prev + 10);
        var result = process.send(prev -> prev + 1);

        assertEquals(1111, result.getOrThrow());
    }

    @Test
    void concurrentMessages() {
        var process = Process.create(0);

        for (int i = 0; i < 100_000; i++) {
            Thread.startVirtualThread(
                    () -> process.send(prev -> prev + 1));
        }

        await().until(() -> process.value().getOrThrow() == 100_000);
    }
}
