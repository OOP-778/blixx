package dev.oop778.blixx.api.parser.node.adventure;

import dev.oop778.blixx.api.parser.node.BlixxNodeImpl;
import dev.oop778.blixx.api.parser.node.IndexedPlaceholder;
import dev.oop778.blixx.api.parser.node.replacement.UniversalNodeReplacement;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import dev.oop778.blixx.api.tag.BlixxTag;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Predicate;

public class BlixxNodeAdventureImpl extends BlixxNodeImpl {

    public BlixxNodeAdventureImpl(Object key, Component component) {
        super(key, null);
        this.adventureComponent = component;
    }

    @Override
    public BlixxNodeImpl copyMe() {
        return new BlixxNodeAdventureImpl(this.key, this.adventureComponent);
    }

    @Override
    public BlixxNodeImpl createNextNode(@Nullable Predicate<BlixxTag.WithDefinedData<?>> tagFilterer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void replace(Iterable<? extends BlixxPlaceholder<?>> placeholders, PlaceholderContext context) {
        new UniversalNodeReplacement(this,placeholders,context).work();
    }

    @Override
    public void collectSelfPlaceholders(Map<String, IndexedPlaceholder> indexedPlaceholderMap) {}
}
