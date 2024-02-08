package dev.yila.process;

import org.junit.jupiter.api.Test;

import java.util.Stack;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

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

    @Test
    void canStartOneMillionProcesses() {
        var stack = new Stack<Process<Integer>>();
        for (int i = 0; i < 1_000_000; i++) {
            stack.push(Process.create(0));
        }
        while (!stack.isEmpty()) {
            stack.pop().stop();
        }
    }

    @Test
    void startAndStop() {
        var process = Process.create(21);

        var result = process.stop();

        assertEquals(21, result.getOrThrow());
    }

    @Test
    void exceptionInFirstMessage() {
        var process = Process.create(0);
        var runtimeException = new RuntimeException();

        var firstResult = process.send(prev -> {
            throw runtimeException;
        });
        var result = process.send(prev -> prev + 100);

        assertSame(runtimeException, firstResult.failure().get().toThrowable());
        assertEquals(100, result.getOrThrow());
    }

    @Test
    void exceptionInLastMessage() {
        var process = Process.create(0);
        var runtimeException = new RuntimeException();

        var result = process.send(prev -> prev + 150);
        var lastResult = process.send(prev -> {
            throw runtimeException;
        });

        assertSame(runtimeException, lastResult.failure().get().toThrowable());
        assertEquals(150, result.getOrThrow());
    }
}
