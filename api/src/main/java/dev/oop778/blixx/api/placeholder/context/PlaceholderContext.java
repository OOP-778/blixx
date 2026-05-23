package dev.oop778.blixx.api.placeholder.context;

import dev.oop778.blixx.api.spi.BlixxFactoryHolder;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.CheckReturnValue;

public interface PlaceholderContext {
    static PlaceholderContext compose(Collection<PlaceholderContext> contexts) {
        return BlixxFactoryHolder.get().composePlaceholderContext(contexts);
    }

    static PlaceholderContext compose(PlaceholderContext... contexts) {
        return BlixxFactoryHolder.get().composePlaceholderContext(contexts);
    }

    static PlaceholderContext create(Object... objects) {
        return BlixxFactoryHolder.get().createPlaceholderContext(objects);
    }

    static PlaceholderContext createWithInheritance(Object... objects) {
        return BlixxFactoryHolder.get().createPlaceholderContextWithInheritance(objects);
    }

    <T> Optional<T> find(Class<T> clazz);

    <T> List<T> findAll(Class<T> clazz);

    @CheckReturnValue
    PlaceholderContext withExact(Object object);

    @CheckReturnValue
    PlaceholderContext withInheritance(Object object);

    @CheckReturnValue
    default PlaceholderContext with(PlaceholderContext context) {
        return compose(context, this);
    }
}
