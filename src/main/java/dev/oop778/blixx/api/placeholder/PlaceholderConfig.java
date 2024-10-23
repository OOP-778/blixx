package dev.oop778.blixx.api.placeholder;

import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;

import java.util.Map;
import java.util.function.Function;

public interface PlaceholderConfig {
    FormatterConfig formatter();
    DefaultContextConfig defaultContext();

    interface FormatterConfig {
        Map<Class<?>, Function<?, Object>> formatters();
    }

    interface DefaultContextConfig {
        PlaceholderContext context();
    }
}
