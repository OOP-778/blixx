package dev.oop778.blixx.api.placeholder.context;

import lombok.NonNull;
import org.jetbrains.annotations.ApiStatus;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

@ApiStatus.Internal
@SuppressWarnings("unchecked")
public class PlaceholderContextImpl implements PlaceholderContext {
    private final Map<Class<?>, Reference<?>> registered = new IdentityHashMap<>();

    @Override
    public <T> Optional<T> find(Class<T> clazz) {
        return Optional
                .ofNullable((T) this.registered.get(clazz))
                .map(ref -> (T) ((Reference<?>) ref).get());
    }

    public <T> void register(@NonNull T object) {
        this.registered.put(object.getClass(), new WeakReference<>(object));
    }

    public <T> void registerForHierarchy(@NonNull T object) {
        Class<?> clazz = object.getClass();
        final WeakReference<T> reference = new WeakReference<>(object);

        while (clazz != null && clazz != Object.class) {
            for (final Class<?> iface : object.getClass().getInterfaces()) {
                this.registered.put(iface, reference);
            }

            this.registered.put(clazz, reference);
            clazz = clazz.getSuperclass();
        }
    }

    protected static class Composed implements PlaceholderContext {
        private final PlaceholderContext[] contexts;

        public Composed(PlaceholderContext[] contexts) {
            this.contexts = contexts;
        }

        @Override
        public <T> Optional<T> find(Class<T> clazz) {
            for (final PlaceholderContext context : this.contexts) {
                if (context == null) {
                    continue;
                }

                final Optional<T> optional = context.find(clazz);
                if (optional.isPresent()) {
                    return optional;
                }
            }

            return Optional.empty();
        }
    }
}
