package dev.oop778.blixx.adventure.node;

import dev.oop778.blixx.api.parser.indexable.Indexable;
import dev.oop778.blixx.api.parser.node.AbstractBlixxNode;
import dev.oop778.blixx.api.parser.node.BlixxNode;
import dev.oop778.blixx.api.parser.node.BlixxNodeInternal;
import dev.oop778.blixx.api.parser.node.BlixxNodeWithPrebuilt;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

public class BlixxNodeAdventureImpl extends AbstractBlixxNode
        implements Indexable.WithNodeContent<BlixxNodeInternal>, BlixxNodeWithPrebuilt {
    private final Component component;

    public BlixxNodeAdventureImpl(Component component) {
        this.component = component;
    }

    @Override
    public BlixxNodeAdventureImpl copyMe() {
        return new BlixxNodeAdventureImpl(this.component);
    }

    @Override
    public BlixxNode getNode() {
        return this;
    }

    @Override
    public @Nullable Object getPreBuilt() {
        return this.component;
    }

    @Override
    public void prebuild() {}
}
