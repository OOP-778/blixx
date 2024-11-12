package dev.oop778.blixx.util.adventure;

import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;

import java.util.*;

public class AdventureComponentSplitter {
    private final List<Component> result = new ArrayList<>();
    private final Component at;
    private final Component what;
    private final Deque<Component> stack = new ArrayDeque<>();
    private Component builder;

    private AdventureComponentSplitter(Component at, Component what) {
        this.at = at;
        this.what = what;
        this.builder = this.newBuilder();
        this.stack.push(what);
        this.work();
    }

    public static List<Component> split(@NonNull Component at, @NonNull Component what) {
        return new AdventureComponentSplitter(at, what).getResult();
    }

    public List<Component> getResult() {
        if (!this.builder.equals(Component.empty())) {
            this.result.add(this.builder);
        }

        if (this.result.isEmpty()) {
            this.result.add(this.what);
        }

        return this.result;
    }

    private void work() {
        while (!this.stack.isEmpty()) {
            final Component next = this.stack.pop();

            if (this.shouldSplitNormalNewLine(next)) {
                this.endLine();
                continue;
            }

            if (this.shouldSplitNewLineWithChildren(next)) {
                this.endLine();
                this.addChildrenToStack(next);
                continue;
            }

            if (this.shouldSplitNewLineInContent(next)) {
                this.splitContent((TextComponent) next);
                continue;
            }

            if (!next.children().isEmpty()) {
                final List<Component> children = next.children();
                for (int i = children.size() - 1; i >= 0; i--) {
                    this.stack.push(children.get(i));
                }
            }

            this.appendMergedComponent(next);
        }
    }

    private boolean shouldSplitNormalNewLine(Component component) {
        return component.children().isEmpty() && component.equals(this.at);
    }

    private boolean shouldSplitNewLineWithChildren(Component component) {
        return !component.children().isEmpty() && component.children(Collections.emptyList()).equals(this.at);
    }

    private boolean shouldSplitNewLineInContent(Component component) {
        return this.at instanceof TextComponent && component instanceof TextComponent &&
                ((TextComponent) component).content().contains(((TextComponent) this.at).content());
    }

    private void splitContent(TextComponent component) {
        final String atText = ((TextComponent) this.at).content();
        final String[] splitParts = component.content().split(atText, -1); // Use -1 to include trailing empty parts

        // If no split occurred, treat it as a single segment
        if (splitParts.length == 1 && splitParts[0].equals(component.content())) {
            this.builder = this.builder.append(Component.text(component.content()).style(component.style()));
            return;
        }

        for (int i = 0; i < splitParts.length; i++) {
            final String part = splitParts[i];
            if (!part.isEmpty()) {
                this.builder = this.builder.append(Component.text(part).style(component.style()));
            }

            // Add newline after each segment except the last one
            if (i < splitParts.length - 1) {
                this.endLine();
            }
        }
    }

    private void addNewLines(String content) {
        final int newLines = (int) content.chars().filter(ch -> ch == '\n').count();
        for (int i = 0; i < newLines; i++) {
            this.endLine();
        }
    }

    private void appendMergedComponent(Component component) {
        final Component merged = component.children(Collections.emptyList())
                .style(component.style().merge(this.builder.style(), Style.Merge.Strategy.IF_ABSENT_ON_TARGET));
        this.builder = this.builder.append(merged);
    }

    private void endLine() {
        this.result.add(this.builder);
        this.builder = this.newBuilder();
    }

    private Component newBuilder() {
        return Component.empty().style(this.what.style());
    }

    private void addChildrenToStack(Component parent) {
        for (final Component child : parent.children()) {
            this.stack.push(child);
        }
    }

    private static class ComponentWrapper {
        final Component component;
        final boolean withSelf;

        ComponentWrapper(Component component, boolean withSelf) {
            this.component = component;
            this.withSelf = withSelf;
        }
    }
}
