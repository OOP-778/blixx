package dev.oop778.blixx.api.parser.node.identityspec;

import dev.oop778.blixx.api.Blixx;
import dev.oop778.blixx.api.parser.indexable.Indexable;
import dev.oop778.blixx.api.parser.node.BlixxNode;
import dev.oop778.blixx.api.parser.node.BlixxNodeSpec;
import dev.oop778.blixx.api.parser.node.keyedspec.BlixxKeyedNodeSpec;
import dev.oop778.blixx.api.tag.BlixxTag;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.*;

@Getter
public class BlixxIdentitySpec implements BlixxNodeSpec {
    private final Blixx blixx;
    @Nullable
    private Map<String, List<Indexable>> indexables;
    @Nullable
    private Set<Indexable> hasPlaceholders;

    public BlixxIdentitySpec(Blixx blixx) {
        this.blixx = blixx;
    }

    @Override
    public void indexPlaceholders(Indexable indexable, Blixx blixx) {
        final Indexable unwrapped = this.unwrapNodeIfWrapped(indexable);

        BlixxKeyedNodeSpec.findNewPlaceholders(indexable, blixx.parserConfig().placeholderFormats(), (placeholder, $) -> this.getOrCreateIndexables().compute(placeholder, (key, value) -> {
            this.hasPlaceholders.add(indexable);

            if (value == null) {
                return new ArrayList<>(Arrays.asList(indexable));
            }

            value.add(indexable);
            return value;
        }));

        if (unwrapped instanceof BlixxNode) {
            this.indexTagPlaceholders((BlixxNode) unwrapped, blixx);
        }
    }

    @Override
    public boolean hasPlaceholders(Indexable indexable, boolean withTags) {
        if (this.indexables == null) {
            return false;
        }

        final Indexable unwrapped = this.unwrapNodeIfWrapped(indexable);

        final boolean result = this.hasPlaceholders.contains(indexable);
        if (result || (indexable instanceof BlixxNode && !((BlixxNode) indexable).isHasIndexableTagData())) {
            return true;
        }

        if (!withTags) {
            return false;
        }

        return unwrapped instanceof BlixxNode && this.checkForIndexedTags((BlixxNode) unwrapped);
    }

    @Override
    public BlixxNode createNode() {
        return new BlixxIdentityNodeImpl(new BlixxIdentitySpec(this.blixx));
    }

    @Override
    public @UnknownNullability Object createNodeKey() {
        return null;
    }

    public Map<String, List<Indexable>> getIndexables() {
        return this.indexables != null ? this.indexables : Collections.emptyMap();
    }

    public BlixxIdentitySpec copy(Map<Indexable, Indexable> copies) {
        if (this.indexables == null) {
            return new BlixxIdentitySpec(this.blixx);
        }

        final BlixxIdentitySpec copy = new BlixxIdentitySpec(this.blixx);
        for (final String placeholder : this.indexables.keySet()) {
            final List<Indexable> values = this.indexables.get(placeholder);
            for (final Indexable value : values) {
                Indexable newValue = copies.get(value);
                if (newValue == null) {
                    newValue = value.copy();
                }

                copy.addIndexable(placeholder, newValue);
            }
        }

        return copy;
    }

    private Map<String, List<Indexable>> getOrCreateIndexables() {
        if (this.indexables != null) {
            return this.indexables;
        }

        this.indexables = new HashMap<>(10);
        this.hasPlaceholders = Collections.newSetFromMap(new IdentityHashMap<>(2));

        return this.indexables;
    }

    private void addIndexable(String key, Indexable indexable) {
        this.getOrCreateIndexables().computeIfAbsent(key, $ -> new ArrayList<>(2)).add(indexable);
        this.hasPlaceholders.add(indexable);
    }

    private boolean checkForIndexedTags(BlixxNode indexable) {
        for (final BlixxTag.WithDefinedData<?> tag : indexable.getTags()) {
            final Object definedData = tag.getDefinedData();
            if (!(definedData instanceof Indexable)) {
                continue;
            }

            if (this.hasPlaceholders.contains((Indexable) definedData)) {
                return true;
            }
        }

        return false;
    }

    private Indexable unwrapNodeIfWrapped(Indexable indexable) {
        return indexable instanceof BlixxNode.WithNodeContent ? ((BlixxNode.WithNodeContent) indexable).getNode() : indexable;
    }

    private void indexTagPlaceholders(BlixxNode node, Blixx blixx) {
        if (!node.isHasIndexableTagData()) {
            return;
        }

        final Iterable<BlixxTag.WithDefinedData<?>> tags = node.getTags();
        if (tags == null) {
            return;
        }

        for (final BlixxTag.WithDefinedData<?> tag : tags) {
            final Object definedData = tag.getDefinedData();
            if (!(definedData instanceof Indexable)) {
                continue;
            }

            this.indexPlaceholders((Indexable) definedData, blixx);
        }
    }
}
