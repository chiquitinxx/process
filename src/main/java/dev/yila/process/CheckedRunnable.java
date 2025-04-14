package dev.yila.process;

@FunctionalInterface
public interface CheckedRunnable<T extends Throwable> {
    void run() throws T;

    static CheckedRunnable<RuntimeException> from(Runnable runnable) {
        return () -> {
            try {
                runnable.run();
            } catch (RuntimeException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
