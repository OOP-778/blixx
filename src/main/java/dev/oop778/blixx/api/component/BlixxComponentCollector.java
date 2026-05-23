package dev.oop778.blixx.api.component;

import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import lombok.RequiredArgsConstructor;

/** {@link Collector} that joins {@link BlixxComponent}s with a delimiter, for use with {@link java.util.stream.Stream#collect}. */
@RequiredArgsConstructor
public class BlixxComponentCollector
        implements Collector<BlixxComponent, BlixxComponentCollector.State, BlixxComponent> {
    private final BlixxComponent delimiter;
    private final boolean includeAtTheEnd;

    @Override
    public Supplier<State> supplier() {
        return State::new;
    }

    @Override
    public BiConsumer<State, BlixxComponent> accumulator() {
        return (state, component) -> {
            if (state.result == null) {
                state.result = component.copy();
            } else {
                state.result = state.result.append(this.delimiter).append(component);
            }
        };
    }

    @Override
    public BinaryOperator<State> combiner() {
        return (state1, state2) -> {
            if (state1.result == null) {
                return state2;
            }

            if (state2.result == null) {
                return state1;
            }

            state1.result = state1.result.append(this.delimiter).append(state2.result);
            return state1;
        };
    }

    @Override
    public Function<State, BlixxComponent> finisher() {
        return (joinState) -> {
            if (joinState.result == null) {
                return BlixxComponent.empty();
            }

            if (this.includeAtTheEnd) {
                joinState.result = joinState.result.append(this.delimiter);
            }

            return joinState.result;
        };
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Collections.emptySet();
    }

    public static class State {
        private BlixxComponent result;
    }
}
