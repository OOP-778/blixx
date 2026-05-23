package dev.oop778.blixx.api.spi;

import dev.oop778.blixx.api.Blixx;
import dev.oop778.blixx.api.BlixxBuilder;
import dev.oop778.blixx.api.BlixxBuilderImpl;
import dev.oop778.blixx.api.component.BlixxComponent;
import dev.oop778.blixx.api.component.BlixxComponentCollector;
import dev.oop778.blixx.api.component.BlixxComponentImpl;
import dev.oop778.blixx.api.formatter.BlixxFormatters;
import dev.oop778.blixx.api.formatter.BlixxFormattersImpl;
import dev.oop778.blixx.api.parser.node.BlixxNode;
import dev.oop778.blixx.api.parser.node.kind.BlixxTextNodeImpl;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholderBuilder;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholderBuilderImpl;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContextImpl;
import dev.oop778.blixx.api.replacer.PlaceholderHolder;
import dev.oop778.blixx.api.replacer.composed.ComposedReplacer;
import dev.oop778.blixx.api.replacer.composed.ComposedReplacerImpl;
import dev.oop778.blixx.api.replacer.immutable.Replacer;
import dev.oop778.blixx.api.replacer.immutable.ReplacerImpl;
import dev.oop778.blixx.api.replacer.mutable.MutableReplacer;
import dev.oop778.blixx.api.replacer.mutable.MutableReplacerImpl;
import dev.oop778.blixx.util.UnsafeCast;
import dev.oop778.blixx.util.collection.ObjectArray;
import dev.oop778.blixx.util.inheritance.InheritanceRegistry;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.function.Function;
import java.util.stream.Collector;

public class BlixxFactoryImpl implements BlixxFactory {
    private volatile Blixx standardInstance;

    @Override
    public BlixxBuilder.ParserSelectorPart<BlixxBuilder.PlaceholderSelectorPart<BlixxBuilder.OptionalStep>>
            createBuilder() {
        return UnsafeCast.cast(new BlixxBuilderImpl());
    }

    @Override
    public Blixx createStandard() {
        if (this.standardInstance == null) {
            synchronized (this) {
                if (this.standardInstance == null) {
                    this.standardInstance = Blixx.builder()
                            .withStandardParserConfig((config) -> config.withPlaceholderFormat('{', '}'))
                            .withStandardPlaceholderConfig()
                            .build();
                }
            }
        }
        return this.standardInstance;
    }

    @Override
    public BlixxComponent createComponent(BlixxNode node) {
        return new BlixxComponentImpl(node);
    }

    @Override
    public BlixxComponent createEmptyComponent() {
        return new BlixxComponentImpl(new BlixxTextNodeImpl("", ObjectArray.empty()));
    }

    @Override
    public BlixxComponent createSpaceComponent() {
        return new BlixxComponentImpl(new BlixxTextNodeImpl(" ", ObjectArray.empty()));
    }

    @Override
    public BlixxComponent createNewLineComponent() {
        return new BlixxComponentImpl(new BlixxTextNodeImpl("\n", ObjectArray.empty()));
    }

    @Override
    public Collector<? super BlixxComponent, ?, BlixxComponent> createComponentCollector(
            BlixxComponent delimiter, boolean includeAtEnd) {
        return new BlixxComponentCollector(delimiter, includeAtEnd);
    }

    @Override
    public Replacer createImmutableReplacer() {
        return new ReplacerImpl();
    }

    @Override
    public Replacer createImmutableReplacer(BlixxPlaceholder<?>... placeholders) {
        return new ReplacerImpl(placeholders);
    }

    @Override
    public MutableReplacer createMutableReplacer() {
        return new MutableReplacerImpl();
    }

    @Override
    public MutableReplacer createMutableReplacer(BlixxPlaceholder<?>... placeholders) {
        return new MutableReplacerImpl(placeholders);
    }

    @Override
    public ComposedReplacer createComposedReplacer(PlaceholderHolder<?>... holders) {
        return new ComposedReplacerImpl(holders);
    }

    @Override
    public <BUILD_TARGET, T> BlixxPlaceholderBuilder.SelectorStage<BUILD_TARGET, T> createPlaceholderBuilder(
            Function<BlixxPlaceholder<?>, BUILD_TARGET> postBuildFunction) {
        return new BlixxPlaceholderBuilderImpl.SelectorStageImpl<>(postBuildFunction);
    }

    @Override
    public PlaceholderContext composePlaceholderContext(PlaceholderContext... contexts) {
        return new PlaceholderContextImpl.Composed(contexts);
    }

    @Override
    public PlaceholderContext composePlaceholderContext(Collection<PlaceholderContext> contexts) {
        return new PlaceholderContextImpl.Composed(contexts.toArray(new PlaceholderContext[0]));
    }

    @Override
    public PlaceholderContext createPlaceholderContext(Object... objects) {
        final PlaceholderContextImpl ctx = new PlaceholderContextImpl();
        for (final Object object : objects) {
            ctx.register(object);
        }
        return ctx;
    }

    @Override
    public PlaceholderContext createPlaceholderContextWithInheritance(Object... objects) {
        final PlaceholderContextImpl ctx = new PlaceholderContextImpl();
        for (final Object object : objects) {
            ctx.registerForHierarchy(object);
        }
        return ctx;
    }

    @Override
    public BlixxFormatters createFormatters() {
        return new BlixxFormattersImpl(new InheritanceRegistry<>(() -> new IdentityHashMap<>()));
    }
}
