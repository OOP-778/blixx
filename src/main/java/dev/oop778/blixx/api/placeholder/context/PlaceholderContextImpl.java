package dev.oop778.blixx.api.placeholder.context;

import dev.oop778.blixx.util.inheritance.InheritanceRegistry;
import lombok.NonNull;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;

@ApiStatus.Internal
@SuppressWarnings("unchecked")
public class PlaceholderContextImpl implements PlaceholderContext {
    private final InheritanceRegistry<Object> registry;

    public PlaceholderContextImpl() {
        this(2);
    }

    public PlaceholderContextImpl(InheritanceRegistry<Object> registry) {
        this.registry = registry;
    }

    public PlaceholderContextImpl(int size) {
        this.registry = new InheritanceRegistry<>(() -> new IdentityHashMap<>(size));
    }

    @Override
    public <T> Optional<T> find(Class<T> clazz) {
        return Optional.ofNullable(this.registry.get(clazz));
    }

    @Override
    public <T> List<T> findAll(Class<T> clazz) {
        return Collections.emptyList();
    }

    @Override
    public PlaceholderContext withExact(Object object) {
        return this.createChildren().register(object);
    }

    @Override
    public PlaceholderContext withInheritance(Object object) {
        return this.createChildren().registerForHierarchy(object);
    }

    public <T> PlaceholderContextImpl register(@NonNull T object) {
        this.registry.registerExact(object.getClass(), object);
        return this;
    }

    public <T> PlaceholderContextImpl registerForHierarchy(@NonNull T object) {
        this.registry.registerWithInheritance(object.getClass(), object);
        return this;
    }

    private PlaceholderContextImpl createChildren() {
        return new PlaceholderContextImpl(this.registry.createChildren());
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

        @Override
        public <T> List<T> findAll(Class<T> clazz) {
            final List<T> list = new ArrayList<>();
            final Set<T> visited = Collections.newSetFromMap(new IdentityHashMap<>());

            final Queue<PlaceholderContext> queue = new LinkedList<>();
            queue.add(this);

            while (!queue.isEmpty()) {
                final PlaceholderContext poll = queue.poll();
                if (poll instanceof Composed) {
                    queue.addAll(Arrays.asList(((Composed) poll).contexts));
                    continue;
                }

                final Optional<T> optional = poll.find(clazz);
                if (optional.isPresent()) {
                    final T t = optional.get();
                    if (visited.add(t)) {
                        list.add(t);
                    }
                }

            }

            return list;
        }

        @Override
        public PlaceholderContext withExact(Object object) {
            return PlaceholderContext.compose(PlaceholderContext.create(object), this);
        }

        @Override
        public PlaceholderContext withInheritance(Object object) {
            return PlaceholderContext.compose(PlaceholderContext.createWithInheritance(object), this);
        }
    }
}
