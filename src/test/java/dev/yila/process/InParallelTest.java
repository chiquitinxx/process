package dev.yila.process;

import dev.yila.functional.DirectResult;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class InParallelTest {

    @Test
    public void executeInParallel() {
        var list = DirectResult.ok(List.of(10, 2, 3, 12, 8, 15, 20));

        var start = LocalDateTime.now();
        var result = InParallel.each(list, number -> {
            try {
                Thread.sleep(number * 100);
                return number * 2;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).getOrThrow();
        var millis = ChronoUnit.MILLIS.between(start, LocalDateTime.now());

        assertThat(millis).isLessThan(2500);
        assertEquals(140, result.stream().mapToInt(Integer::intValue).sum());
    }

    @Test
    public void failFast() {
        var list = DirectResult.ok(List.of(50, 1, 20));

        var start = LocalDateTime.now();
        var failure = InParallel.each(list, number -> {
            try {
                if (number == 1) {
                    throw new RuntimeException("one");
                }
                Thread.sleep(number * 100);
                return number * 2;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).failure().get();
        var millis = ChronoUnit.MILLIS.between(start, LocalDateTime.now());

        assertThat(millis).isLessThan(100);
        assertEquals("one", failure.toThrowable().getMessage());
    }
}
