package dev.yila.process;

import org.junit.jupiter.api.Test;

import java.util.Stack;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class StateProcessTest {

    @Test
    void creation() {
        var process = StateProcess.create(0);

        var result = process.apply(prev -> prev + 1);

        assertEquals(1, result.getOrThrow());
    }

    @Test
    void applyMultipleFunctions() {
        var process = StateProcess.create(0);

        process.apply(prev -> prev + 1000);
        process.apply(prev -> prev + 100);
        process.apply(prev -> prev + 10);
        var result = process.apply(prev -> prev + 1);

        assertEquals(1111, result.getOrThrow());
    }

    @Test
    void concurrentApplies() {
        var process = StateProcess.create(0);

        for (int i = 0; i < 100_000; i++) {
            Thread.startVirtualThread(() -> process.apply(prev -> prev + 1));
        }

        await().until(() -> process.value().getOrThrow() == 100_000);
        process.stop();
    }

    @Test
    void canStartOneMillionProcesses() {
        var stack = new Stack<StateProcess<Integer>>();
        for (int i = 0; i < 1_000_000; i++) {
            stack.push(StateProcess.create(0));
        }
        while (!stack.isEmpty()) {
            stack.pop().stop();
        }
    }

    @Test
    void startAndStop() {
        var process = StateProcess.create(21);

        var result = process.stop();

        assertEquals(21, result.getOrThrow());
    }

    @Test
    void exceptionInFirstApply() {
        var process = StateProcess.create(0);
        var runtimeException = new RuntimeException();

        var firstResult = process.apply(prev -> {
            throw runtimeException;
        });
        var result = process.apply(prev -> prev + 100);

        assertSame(runtimeException, firstResult.failure().get().toThrowable());
        assertEquals(100, result.getOrThrow());
    }

    @Test
    void exceptionInLastApply() {
        var process = StateProcess.create(0);
        var runtimeException = new RuntimeException();

        var result = process.apply(prev -> prev + 150);
        var lastResult = process.apply(prev -> {
            throw runtimeException;
        });

        assertSame(runtimeException, lastResult.failure().get().toThrowable());
        assertEquals(150, result.getOrThrow());
    }
}
