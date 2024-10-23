package dev.oop778.blixx.api.placeholder;

import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.function.Function;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public class PlaceholderConfigImpl implements PlaceholderConfig, PlaceholderConfig.FormatterConfig, PlaceholderConfig.DefaultContextConfig {
    private final Map<Class<?>, Function<?, Object>> formatters;
    private final PlaceholderContext context;

    @Override
    public FormatterConfig formatter() {
        return this;
    }

    @Override
    public DefaultContextConfig defaultContext() {
        return this;
    }
}
