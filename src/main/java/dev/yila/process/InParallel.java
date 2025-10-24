package dev.yila.process;

import dev.yila.functional.DirectResult;
import dev.yila.functional.Result;

import java.util.List;
import java.util.concurrent.StructuredTaskScope;
import java.util.function.Function;

public class InParallel {
    public static <T, R> Result<List<R>> each(Result<List<T>> list, Function<T, R> function) {
        return list.flatMap(l -> {
            try (var scope = StructuredTaskScope.open(
                    StructuredTaskScope.Joiner.allSuccessfulOrThrow())) {
                var suppliers = l.stream().map(e -> scope.fork(() -> function.apply(e))).toList();
                scope.join();
                return DirectResult.ok(suppliers.stream().map(StructuredTaskScope.Subtask::get).toList());
            } catch (InterruptedException e) {
                return DirectResult.failure(e);
            } catch (StructuredTaskScope.FailedException e) {
                return DirectResult.failure(e.getCause());
            }
        });
    }
}
