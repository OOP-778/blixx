package dev.oop778.blixx.api.parser.node.identityspec;

import dev.oop778.blixx.api.parser.indexable.Indexable;
import dev.oop778.blixx.api.parser.node.BlixxNode;
import dev.oop778.blixx.api.parser.node.BlixxNodeImpl;
import dev.oop778.blixx.api.parser.node.BlixxNodeSpec;
import dev.oop778.blixx.api.parser.node.keyedspec.BlixxKeyedNodeSpec;
import dev.oop778.blixx.api.parser.node.replacement.AbstractNodeReplacement;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class NodeReplacementIdentity extends AbstractNodeReplacement {
    private final List<Indexable> toRevisit = new ArrayList<>();

    public NodeReplacementIdentity(BlixxNodeImpl rootNode, Iterable<? extends BlixxPlaceholder<?>> placeholders, PlaceholderContext context) {
        super(rootNode, placeholders, context);
    }

    @Override
    public void work() {
        final ComposedIndexableMap composedIndexableMap = this.collectPlaceholders();
        this.doWork(composedIndexableMap);

        if (this.toRevisit.isEmpty()) {
            return;
        }

        while (!this.toRevisit.isEmpty()) {
            final ComposedIndexableMap composedIndexableMap2 = this.collectNewPlaceholders();
            this.toRevisit.clear();

            this.doWork(composedIndexableMap2);
        }
    }

    @Override
    protected void postSuccessfulReplacement(Indexable node) {
        if (!BlixxKeyedNodeSpec.findAnyPlaceholders(node, this.rootNode.getSpec().getBlixx().parserConfig().placeholderFormats())) {
            return;
        }

        this.toRevisit.add(node);
    }

    public void doWork(ComposedIndexableMap composedIndexableMap) {
        final Iterator<Map.Entry<String, Iterable<Indexable>>> iterator = composedIndexableMap.iterator();

        while (iterator.hasNext()) {
            final Map.Entry<String, Iterable<Indexable>> entry = iterator.next();
            final String fullPlaceholder = entry.getKey();

            for (final BlixxPlaceholder<?> blixxPlaceholder : this.placeholders) {
                if (blixxPlaceholder instanceof BlixxPlaceholder.Literal) {
                    this.handleLiteralReplacement(fullPlaceholder, entry.getValue(), (BlixxPlaceholder.Literal<?>) blixxPlaceholder, this.context);
                    continue;
                }

                if (blixxPlaceholder instanceof BlixxPlaceholder.Pattern) {
                    this.handlePatternReplacement(fullPlaceholder, entry.getValue(), (BlixxPlaceholder.Pattern<?>) blixxPlaceholder, this.context);
                }
            }
        }
    }

    private ComposedIndexableMap collectNewPlaceholders() {
        final ComposedIndexableMap composedIndexableMap = new ComposedIndexableMap(0);
        final Iterator<Indexable> iterator = this.toRevisit.iterator();

        final Map<String, List<Indexable>> newIndexables = new HashMap<>();

        while (iterator.hasNext()) {
            final Indexable next = iterator.next();
            if (next instanceof Indexable.WithNodeContent) {
                final BlixxNode node = ((Indexable.WithNodeContent) next).getNode();
                final BlixxNodeSpec spec = node.getSpec();

                if (spec instanceof BlixxIdentitySpec) {
                    final Map<String, List<Indexable>> indexables = ((BlixxIdentitySpec) spec).getIndexables();
                    if (!indexables.isEmpty()) {
                        composedIndexableMap.add(indexables);
                    }
                }
            }

            BlixxKeyedNodeSpec.findNewPlaceholders(next, this.rootNode.getSpec().getBlixx().parserConfig().placeholderFormats(), (placeholder, $) -> {
                newIndexables.computeIfAbsent(placeholder, ($2) -> new ArrayList<>(2)).add(next);
            });
        }

        composedIndexableMap.add(newIndexables);
        return composedIndexableMap;
    }

    private ComposedIndexableMap collectPlaceholders() {
        final Iterator<BlixxNodeImpl> iterator = this.rootNode.iterator(true);
        final ComposedIndexableMap composedIndexableMap = new ComposedIndexableMap(0);

        while (iterator.hasNext()) {
            final BlixxNodeImpl node = iterator.next();
            final BlixxNodeSpec spec = node.getSpec();

            if (spec instanceof BlixxIdentitySpec) {
                final Map<String, List<Indexable>> indexables = ((BlixxIdentitySpec) spec).getIndexables();
                if (!indexables.isEmpty()) {
                    composedIndexableMap.add(indexables);
                }
            }
        }

        return composedIndexableMap;
    }

    public static class ComposedIndexableMap {
        private final Set<String> keys;
        private Map<String, ? extends Iterable<Indexable>>[] composed;

        public ComposedIndexableMap(int size) {
            this.composed = new Map[size];
            this.keys = new HashSet<>();
        }

        public void add(Map<String, ? extends Iterable<Indexable>> map) {
            final int where = this.composed.length;
            this.composed = Arrays.copyOf(
                    this.composed,
                    this.composed.length + 1);
            this.composed[where] = map;
            this.keys.addAll(map.keySet());
        }

        public Iterator<Map.Entry<String, Iterable<Indexable>>> iterator() {
            return new ComposedIterator();
        }

        private class ComposedIterator implements Iterator<Map.Entry<String, Iterable<Indexable>>> {
            private final Iterator<String> keyIterator;
            private Map.Entry<String, Iterable<Indexable>> current;

            public ComposedIterator() {
                this.keyIterator = ComposedIndexableMap.this.keys.iterator();
                this.advance();
            }

            @Override
            public boolean hasNext() {
                return this.current != null;
            }

            @Override
            public Map.Entry<String, Iterable<Indexable>> next() {
                if (this.current == null) {
                    throw new NoSuchElementException();
                }

                final Map.Entry<String, Iterable<Indexable>> current = this.current;
                this.advance();

                return current;
            }

            private void advance() {
                if (!this.keyIterator.hasNext()) {
                    this.current = null;
                    return;
                }

                final String key = this.keyIterator.next();
                final List<Iterable<Indexable>> indexables = new ArrayList<>(2);

                for (final Map<String, ? extends Iterable<Indexable>> map : ComposedIndexableMap.this.composed) {
                    final Iterable<Indexable> mapEntry = map.get(key);
                    if (mapEntry != null) {
                        indexables.add(mapEntry);
                    }
                }

                this.current = new AbstractMap.SimpleEntry<>(key, new ComposedIndexables(indexables));
            }
        }

        @RequiredArgsConstructor
        private static class ComposedIndexables implements Iterable<Indexable> {
            private final List<Iterable<Indexable>> indexables;

            @Override
            public @NotNull Iterator<Indexable> iterator() {
                return new FlatIndexableIterator(this.indexables);
            }

            private static class FlatIndexableIterator implements Iterator<Indexable> {
                private final List<Iterable<Indexable>> indexables;
                private int currentIterableIndex;
                private Iterator<Indexable> currentIterator;

                public FlatIndexableIterator(List<Iterable<Indexable>> indexables) {
                    this.indexables = indexables;
                    this.currentIterableIndex = 0;
                    if (!indexables.isEmpty()) {
                        this.currentIterator = indexables.get(0).iterator();
                    }
                }

                @Override
                public boolean hasNext() {
                    while (this.currentIterator != null && !this.currentIterator.hasNext() && this.currentIterableIndex < this.indexables.size() - 1) {
                        this.currentIterableIndex++;
                        this.currentIterator = this.indexables.get(this.currentIterableIndex).iterator();
                    }
                    return this.currentIterator != null && this.currentIterator.hasNext();
                }

                @Override
                public Indexable next() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return this.currentIterator.next();
                }
            }
        }
    }
}
