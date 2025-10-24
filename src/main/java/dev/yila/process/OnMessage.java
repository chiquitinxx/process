package dev.yila.process;

import java.util.function.Function;

public record OnMessage(String name, Function<?, ?> function) {
}
