package dev.oop778.blixx.api.spi;

import dev.oop778.blixx.api.Blixx;
import dev.oop778.blixx.api.BlixxBuilder;
import dev.oop778.blixx.api.component.BlixxComponent;
import dev.oop778.blixx.api.formatter.BlixxFormatters;
import dev.oop778.blixx.api.parser.node.BlixxNode;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholderBuilder;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import dev.oop778.blixx.api.replacer.PlaceholderHolder;
import dev.oop778.blixx.api.replacer.composed.ComposedReplacer;
import dev.oop778.blixx.api.replacer.immutable.Replacer;
import dev.oop778.blixx.api.replacer.mutable.MutableReplacer;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collector;

public interface BlixxFactory {
    BlixxBuilder.ParserSelectorPart<BlixxBuilder.PlaceholderSelectorPart<BlixxBuilder.OptionalStep>> createBuilder();

    Blixx createStandard();

    BlixxComponent createComponent(BlixxNode node);

    BlixxComponent createEmptyComponent();

    BlixxComponent createSpaceComponent();

    BlixxComponent createNewLineComponent();

    Collector<? super BlixxComponent, ?, BlixxComponent> createComponentCollector(
            BlixxComponent delimiter, boolean includeAtEnd);

    Replacer createImmutableReplacer();

    Replacer createImmutableReplacer(BlixxPlaceholder<?>... placeholders);

    MutableReplacer createMutableReplacer();

    MutableReplacer createMutableReplacer(BlixxPlaceholder<?>... placeholders);

    ComposedReplacer createComposedReplacer(PlaceholderHolder<?>... holders);

    <BUILD_TARGET, T> BlixxPlaceholderBuilder.SelectorStage<BUILD_TARGET, T> createPlaceholderBuilder(
            Function<BlixxPlaceholder<?>, BUILD_TARGET> postBuildFunction);

    PlaceholderContext composePlaceholderContext(PlaceholderContext... contexts);

    PlaceholderContext composePlaceholderContext(Collection<PlaceholderContext> contexts);

    PlaceholderContext createPlaceholderContext(Object... objects);

    PlaceholderContext createPlaceholderContextWithInheritance(Object... objects);

    BlixxFormatters createFormatters();
}
