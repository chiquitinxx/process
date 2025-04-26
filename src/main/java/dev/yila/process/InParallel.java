package dev.yila.process;

import dev.yila.functional.DirectResult;
import dev.yila.functional.Result;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;
import java.util.function.Function;

public class InParallel {
    public static <T, R> Result<List<R>> each(Result<List<T>> list, Function<T, R> function) {
        return list.flatMap(l -> {
            try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
                var suppliers = l.stream().map(e -> scope.fork(() -> function.apply(e))).toList();
                scope.join().throwIfFailed();
                return DirectResult.ok(suppliers.stream().map(StructuredTaskScope.Subtask::get).toList());
            } catch (InterruptedException | ExecutionException e) {
                return DirectResult.failure(e);
            }
        });
    }
}
