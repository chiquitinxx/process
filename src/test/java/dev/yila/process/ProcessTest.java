package dev.yila.process;

import org.junit.jupiter.api.Test;

import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ProcessTest {

    @Test
    void creation() {
        var process = Process.create();
        var integer = new AtomicInteger(0);

        process.send(integer::incrementAndGet);

        await().until(() -> integer.get() == 1);
    }

    @Test
    void concurrentEvents() {
        var process = Process.create();
        var integer = new AtomicInteger(0);

        for (int i = 0; i < 100_000; i++) {
            Thread.startVirtualThread(() -> process.send(integer::incrementAndGet));
        }

        await().until(() -> integer.get() == 100_000);
        process.stop();
    }

    @Test
    void canStartOneMillionProcesses() {
        var stack = new Stack<Process>();
        for (int i = 0; i < 1_000_000; i++) {
            stack.push(Process.create());
        }
        while (!stack.isEmpty()) {
            stack.pop().stop();
        }
    }

    @Test
    void processContinueWithRuntimeException() {
        var process = Process.create();
        var runtimeException = new RuntimeException();
        var integer = new AtomicInteger(0);

        process.send(() -> { throw runtimeException; });
        process.send(integer::incrementAndGet);
        await().until(() -> integer.get() == 1);
        process.stop();
    }

    @Test
    void runtimeException() {
        var process = Process.create();
        var runtimeException = new RuntimeException();
        var integer = new AtomicInteger(0);

        process.send(() -> { throw runtimeException; }, re -> {
            if (re == runtimeException) {
                integer.set(-1);
            }
        });
        await().until(() -> integer.get() == -1);
        process.stop();
    }
}
