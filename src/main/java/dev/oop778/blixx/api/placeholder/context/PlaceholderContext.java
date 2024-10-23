package dev.oop778.blixx.api.placeholder.context;

import java.util.Collection;
import java.util.Optional;

public interface PlaceholderContext {
    static PlaceholderContext compose(Collection<PlaceholderContext> contexts) {
        return new PlaceholderContextImpl.Composed(contexts.toArray(new PlaceholderContext[0]));
    }

    static PlaceholderContext compose(PlaceholderContext... contexts) {
        return new PlaceholderContextImpl.Composed(contexts);
    }

    static PlaceholderContext create(Object... objects) {
        final PlaceholderContextImpl placeholderContext = new PlaceholderContextImpl();
        for (final Object object : objects) {
            placeholderContext.registerForHierarchy(object);
        }

        return placeholderContext;
    }

    <T> Optional<T> find(Class<T> clazz);
}
