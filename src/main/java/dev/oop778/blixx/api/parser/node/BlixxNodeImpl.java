package dev.oop778.blixx.api.parser.node;

import dev.oop778.blixx.api.Blixx;
import dev.oop778.blixx.api.component.ComponentDecoration;
import dev.oop778.blixx.api.parser.TagWithDefinedDataImpl;
import dev.oop778.blixx.api.parser.indexable.Indexable;
import dev.oop778.blixx.api.placeholder.BlixxPlaceholder;
import dev.oop778.blixx.api.placeholder.context.PlaceholderContext;
import dev.oop778.blixx.api.tag.BlixxProcessor;
import dev.oop778.blixx.api.tag.BlixxTag;
import dev.oop778.blixx.util.adventure.FastComponentBuilder;
import dev.oop778.blixx.util.adventure.StyleBuilder;
import dev.oop778.blixx.util.collection.ObjectArray;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

@Getter
public abstract class BlixxNodeImpl implements BlixxNode {
    protected final Object key;
    @Setter
    protected BlixxNodeSpec spec;
    @Setter
    protected ObjectArray<BlixxTag.WithDefinedData<?>> tags;
    @Setter
    protected BlixxNodeImpl next;
    @Setter
    protected BlixxNodeImpl previous;
    @Setter
    protected String content = "";
    @Nullable
    protected Component adventureComponent;
    protected boolean hasIndexableTagData = false;

    public BlixxNodeImpl(Object key, BlixxNodeSpec spec) {
        this.key = key;
        this.spec = spec;
    }

    @Override
    public ObjectArray<BlixxTag.WithDefinedData<?>> getTags() {
        return this.tags != null ? this.tags : (ObjectArray<BlixxTag.WithDefinedData<?>>) ObjectArray.EMPTY;
    }

    @Override
    public BlixxNodeImpl copy() {
        BlixxNodeImpl top = null;
        BlixxNodeImpl current = this;
        BlixxNodeImpl last = null;

        while (current != null) {
            final BlixxNodeImpl copy = current.copyMe();

            if (top == null) {
                top = copy;
            }

            if (last != null) {
                last.next = copy;
                copy.previous = last;
            }

            current = current.getNext();
            last = copy;
        }

        return top;
    }

    @Override
    public TextComponent build() {
        return this.build(null);
    }

    @Override
    public boolean hasPlaceholders(boolean withTags) {
        return this.spec.hasPlaceholders(this, withTags);
    }

    public abstract BlixxNodeImpl copyMe();

    public abstract BlixxNodeImpl createNextNode(@Nullable Predicate<BlixxTag.WithDefinedData<?>> tagFilterer);

    public abstract void replace(Iterable<? extends BlixxPlaceholder<?>> placeholders, PlaceholderContext context);

    public abstract void collectSelfPlaceholders(Map<String, IndexedPlaceholder> indexedPlaceholderMap);

    public @Nullable Component getAdventureComponent() {
        return this.build();
    }

    public void addTag(BlixxTag.WithDefinedData<?> tag) {
        if (this.tags == null) {
            this.tags = new ObjectArray<>(2);
        }

        if (tag.getDefinedData() instanceof Indexable) {
            this.hasIndexableTagData = true;
        }

        this.tags.add(tag);
    }

    public Iterator<BlixxNodeImpl> iterator(boolean withItself) {
        return new Iterator<BlixxNodeImpl>() {
            private BlixxNodeImpl current = withItself ? BlixxNodeImpl.this : BlixxNodeImpl.this.next;

            @Override
            public boolean hasNext() {
                return this.current != null;
            }

            @Override
            public BlixxNodeImpl next() {
                if (this.current == null) {
                    throw new NoSuchElementException();
                }

                final BlixxNodeImpl nextNode = this.current;
                this.current = this.current.getNext();
                return nextNode;
            }
        };
    }

    public boolean hasTag(BlixxTag.WithDefinedData<?> parsedTag) {
        return this.tags != null && this.tags.stream().anyMatch(parsedTag::compare);
    }

    public boolean hasTag(Predicate<BlixxTag.WithDefinedData<?>> tagFilterer) {
        return this.tags != null && this.tags.stream().anyMatch(tagFilterer);
    }

    public BlixxTag.WithDefinedData<?> findTag(Predicate<BlixxTag.WithDefinedData<?>> tagFilterer) {
        return this.tags == null ? null : this.tags.stream().filter(tagFilterer).findFirst().orElse(null);
    }

    public void parseIntoAdventure() {
        if (this.adventureComponent != null) {
            return;
        }

        this.adventureComponent = this.buildAdventure();
    }

    /**
     * Finds all occurrences of 'what' within this node's content (and content
     * shifted to new nodes due to splitting). For each occurrence:
     * 1. Removes 'what'.
     * 2. Splits the node immediately before the original position of 'what' (if necessary).
     * 3. Applies the tags from the given 'decoration' to the node segment
     * that *originally started* with 'what' (now starts with the content *after* 'what').
     * <p>
     * Only processes the initial 'this' node and nodes subsequently created
     * by the splitting/removal process.
     * <p>
     * Example:
     * Node: "A{tag}B{tag}C", what="{tag}", decoration=Adds<red>
     * Resulting Nodes: "A" -> "<red>B" -> "<red>C"
     *
     * @param what       The string to search for and remove.
     * @param decoration The decoration containing tags to apply where 'what' was found.
     * @return A list of the newly created nodes during the splitting process.
     */
    public List<BlixxNodeImpl> splitRemoveApplyDecoration(String what, ComponentDecoration decoration) {
        // --- Input Validation ---
        if (what == null || what.isEmpty()) {
            return new ArrayList<>();
        }
        if (decoration == null) {
            throw new IllegalArgumentException("Decoration cannot be null.");
        }

        final List<BlixxNodeImpl> newNodes = new ArrayList<>();
        BlixxNodeImpl currentNode = this; // Start ONLY with the initial node

        // Pre-fetch tags (assuming okay based on initial 'this.spec')
        final Iterable<? extends BlixxTag.WithDefinedData<?>> tagsToApply = decoration.getTags(this.spec.getBlixx());

        // Outer loop: Iterates through nodes that need processing
        while (currentNode != null) {
            String currentContent = currentNode.getContent();
            if (currentContent == null) {
                currentNode = null; // Stop processing this path
                continue;
            }

            int searchOffset = 0; // Where to start searching
            BlixxNodeImpl nextNodeToProcess = null; // Track if we jump to a new node

            // Inner loop: Finds all occurrences of 'what' within the currentNode
            // We use a flag because modifying content changes indices.
            boolean modifiedInInnerLoop = false;
            do {
                modifiedInInnerLoop = false; // Reset flag for this pass
                currentContent = currentNode.getContent(); // Get fresh content
                if (currentContent == null || searchOffset >= currentContent.length()) {
                    break; // Nothing left to search in this node
                }

                final int startIndex = currentContent.indexOf(what, searchOffset);

                // --- Case 1: 'what' not found in the remainder ---
                if (startIndex == -1) {
                    break; // Done with this node, exit inner loop.
                }

                // --- 'what' found. Prepare replacement content ---
                final String contentAfterWhat = currentContent.substring(startIndex + what.length());

                // --- Case 2: 'what' found at the current effective start (searchOffset) ---
                if (startIndex == searchOffset) {
                    // Modify current node's content by removing 'what'
                    final String newContent = currentContent.substring(0, startIndex) + contentAfterWhat;

                    /*
                     * EXPERIMENTAL
                     * 1) remove current node
                     * 2) apply the tags to next node
                     * 3) Relink the tree
                     */
                    if (newContent.isEmpty() && currentNode.getNext() != null) {
                        final BlixxNodeImpl next = currentNode.getNext();
                        final BlixxNodeImpl previous = currentNode.getPrevious();

                        // Apply decoration tags to the next node
                        for (final BlixxTag.WithDefinedData<?> tag : tagsToApply) {
                            next.addOrReplaceTag(tag);
                        }

                        // Relink tree
                        // if previous is null, we inherit next into current node
                        if (previous == null) {
                            currentNode.inherit(next);
                            currentNode.setNext(next.getNext());

                            if (next.getNext() != null) {
                                next.getNext().setPrevious(currentNode);
                            }
                            newNodes.add(currentNode);

                        } else {
                            next.setPrevious(previous);
                            previous.setNext(next);
                        }

                        break;
                    }

                    currentNode.setContent(newContent);
                    // Apply decoration tags to this node
                    for (final BlixxTag.WithDefinedData<?> tag : tagsToApply) {
                        currentNode.addOrReplaceTag(tag);
                    }

                    // Content was modified in place. Restart search from the same position
                    // in the *next* pass of the do-while loop. searchOffset remains startIndex.
                    modifiedInInnerLoop = true;

                }
                // --- Case 3: 'what' found after some other text (split needed) ---
                else { // startIndex > searchOffset
                    final String before = currentContent.substring(0, startIndex);
                    // Content for the new node is what comes *after* 'what'
                    final BlixxNodeImpl originalNext = currentNode.getNext();

                    // Create the new node for the part *after* 'what'
                    final BlixxNodeImpl newNode = currentNode.copy(); // Inherit tags/attributes
                    newNode.setContent(contentAfterWhat); // Set content *without* 'what'
                    newNodes.add(newNode);

                    // Apply decoration tags to the NEW node
                    for (final BlixxTag.WithDefinedData<?> tag : tagsToApply) {
                        newNode.addOrReplaceTag(tag);
                    }

                    // Update the original node to only contain the 'before' part
                    currentNode.setContent(before);

                    // Link: currentNode -> newNode -> originalNext
                    currentNode.setNext(newNode);
                    newNode.setPrevious(currentNode);
                    newNode.setNext(originalNext);
                    if (originalNext != null) {
                        originalNext.setPrevious(newNode);
                    }

                    // Set the new node as the one to process next in the outer loop
                    nextNodeToProcess = newNode;
                    // Exit the inner loop (do-while) because content shifted to newNode
                    modifiedInInnerLoop = false; // Ensure we don't loop again for this node
                    break; // Exit do-while, outer loop will handle nextNodeToProcess
                }

                // Update searchOffset for the *next* pass of the do-while, only if we modified in place (Case 2)
                // We search from the same point where the replacement happened.
                searchOffset = startIndex;


            } while (modifiedInInnerLoop); // Loop only if Case 2 happened (in-place modification)


            // Move to the next node determined by splitting (Case 3),
            // or set to null to terminate the outer loop if no split occurred.
            currentNode = nextNodeToProcess;

        } // End outer loop

        return newNodes;
    }

    /**
     * Finds ALL occurrences of 'what' within this node's original content,
     * replacing each with a copy of the 'with' node structure.
     * It splits the current node as necessary. The process only continues
     * into nodes created during this replacement process, not into the
     * original subsequent nodes of the list.
     *
     * @param what The string to search for.
     * @param with The node structure to insert as a replacement.
     * @return A list of the newly created nodes during the replacement process.
     */
    public List<BlixxNodeImpl> splitReplaceRestricted(String what, BlixxNodeImpl with) {
        if (what == null || what.isEmpty()) {
            return new ArrayList<>(); // Nothing to replace
        }
        if (with == null) {
            throw new IllegalArgumentException("Replacement node 'with' cannot be null.");
        }

        final List<BlixxNodeImpl> newNodes = new ArrayList<>();
        BlixxNodeImpl currentNode = this; // Start ONLY with the initial node
        BlixxNodeImpl nodeToRestartLoopWith = null; // Helper to track where to continue

        // Loop focuses only on the current node and nodes created from it.
        while (currentNode != null) {
            nodeToRestartLoopWith = null; // Assume loop will break unless a replacement happens

            // Basic check to prevent infinite loops if 'with' contains 'what'
            // and we land back on a node within the 'with' structure itself.
            if (currentNode == with || this.containsNode(with, currentNode)) {
                break; // Stop processing this path to avoid recursion/loops
            }

            final String input = currentNode.getContent();
            // Handle potential null content gracefully
            if (input == null) {
                break; // Cannot process null content, stop.
            }

            final int startIndex = input.indexOf(what);

            // --- Case 1: 'what' not found in the current node's content ---
            if (startIndex == -1) {
                break; // Stop processing. Do NOT move to the original next node.
            }

            // --- 'what' found, prepare for split/replace ---
            final String before = input.substring(0, startIndex);
            final String after = input.substring(startIndex + what.length());
            // Save the reference to the node *immediately following* the current one
            // *before* modification, as we'll need to link the end of our changes to it.
            final BlixxNodeImpl originalNext = currentNode.getNext();

            // --- Case 2: 'what' matches the entire node content ---
            if (after.isEmpty() && before.isEmpty()) {
                final BlixxNodeImpl copy = with.copy(); // Create a copy of the replacement structure

                if (currentNode.getPrevious() == null) { // Special handling for the head node ('this')
                    // Using 'inherit' as in the original code. Assumes 'this' node's
                    // identity is preserved but its state is replaced.
                    currentNode.inherit(copy);
                    // Decide if the modified head should be in newNodes. Original did this.
                    // If copy() actually returns a distinct new object, maybe that should be added?
                    // Sticking to original for now:
                    newNodes.add(currentNode);
                    currentNode.setNodeAsTreeEnd(originalNext); // Link end of new structure

                    // IMPORTANT: The content of 'currentNode' changed. We need to re-check
                    // this same node in the next iteration.
                    nodeToRestartLoopWith = currentNode;

                } else { // Handling for a non-head node
                    newNodes.add(copy); // Add the newly created copy

                    // Link previous node to the copy
                    final BlixxNodeImpl previousNode = currentNode.getPrevious();
                    previousNode.setNext(copy);
                    copy.setPrevious(previousNode);

                    // Link end of the copy's structure to the original next node
                    copy.setNodeAsTreeEnd(originalNext);

                    // IMPORTANT: Continue processing from the *start* of the inserted
                    // structure ('copy') as it might contain 'what'.
                    nodeToRestartLoopWith = copy;
                }
            }

            // --- Case 3: 'what' found at the end (before is present, after is empty) ---
            else if (after.isEmpty()) { // 'before' must be non-empty here
                final BlixxNodeImpl copy = with.copy();
                newNodes.add(copy);

                // Modify current node's content and link it to the copy
                currentNode.setContent(before);
                currentNode.setNext(copy);
                copy.setPrevious(currentNode);

                // Link the end of the copy's structure to the original next node
                copy.setNodeAsTreeEnd(originalNext);

                // IMPORTANT: Continue processing from the *start* of the inserted
                // structure ('copy').
                nodeToRestartLoopWith = copy;
            }

            // --- Case 4: 'what' found in the middle (before and after are present) ---
            else { // Both 'before' and 'after' have content.
                final BlixxNodeImpl afterNode = currentNode.copy(); // Create a new node for the 'after' part
                afterNode.setContent(after);
                newNodes.add(afterNode);

                final BlixxNodeImpl replacementNode = with.copy(); // Create a copy of the replacement structure
                newNodes.add(replacementNode);

                // Modify current node for the 'before' part and link it
                currentNode.setContent(before);
                currentNode.setNext(replacementNode);
                replacementNode.setPrevious(currentNode);

                // Link the end of the replacement structure to the 'after' node
                final BlixxNodeImpl replacementTreeEnd = replacementNode.findTreeEnd();
                replacementTreeEnd.setNext(afterNode);
                afterNode.setPrevious(replacementTreeEnd);

                // Link the 'after' node to the rest of the original list structure
                afterNode.setNext(originalNext);
                if (originalNext != null) {
                    originalNext.setPrevious(afterNode);
                }

                // IMPORTANT: Continue processing from the 'afterNode', as it contains
                // the remainder of the original content that needs checking.
                nodeToRestartLoopWith = afterNode;
            }

            // Prepare for the next iteration: continue from the node determined above.
            // If nodeToRestartLoopWith is null (e.g., if we hit a break condition that wasn't
            // explicitly handled), the loop will terminate.
            currentNode = nodeToRestartLoopWith;

        } // End while loop that processes replacements originating from 'this'

        return newNodes;
    }

    public List<BlixxNodeImpl> splitReplace(String what, BlixxNodeImpl with) {
        final List<BlixxNodeImpl> newNodes = new ArrayList<>();
        BlixxNodeImpl currentNode = this;

        while (currentNode != null) {
            final String input = currentNode.getContent();
            final int startIndex = input.indexOf(what);

            if (startIndex == -1) {
                currentNode = currentNode.getNext();
                continue;
            }

            // Split the input before and after the found placeholder
            final String before = input.substring(0, startIndex);
            final String after = input.substring(startIndex + what.length());
            final BlixxNodeImpl originalNext = currentNode.getNext(); // copy original next node

            // We just set the node of previous node to copy of with.
            if (after.isEmpty() && before.isEmpty()) {
                final BlixxNodeImpl copy = with.copy();

                // Inherit everything from with copy
                if (currentNode.getPrevious() == null) {
                    currentNode.inherit(copy);
                    newNodes.add(currentNode);
                    currentNode.setNodeAsTreeEnd(originalNext);
                    continue;
                }

                newNodes.add(copy);

                currentNode.getPrevious().setNext(copy);
                copy.setPrevious(currentNode.getPrevious());
                currentNode = copy;

                currentNode.setNodeAsTreeEnd(originalNext);
                continue;
            }

            // After is empty & before is not, we just set content of current node
            if (after.isEmpty()) {
                final BlixxNodeImpl copy = with.copy();
                newNodes.add(copy);

                currentNode.setNext(copy);
                copy.setPrevious(currentNode);

                currentNode.setContent(before);
                currentNode = copy;

                currentNode.setNodeAsTreeEnd(originalNext);
                continue;
            }

            // Both before & after are present
            final BlixxNodeImpl afterNode = currentNode.copy();
            afterNode.setContent(after);
            newNodes.add(afterNode);

            final BlixxNodeImpl beforeNode = with.copy();
            newNodes.add(beforeNode);

            // Set content of current node to before & update nodes
            currentNode.setContent(before);
            currentNode.setNext(beforeNode);
            beforeNode.setPrevious(currentNode);

            final BlixxNodeImpl treeEnd = beforeNode.findTreeEnd();
            treeEnd.setNext(afterNode);
            afterNode.setPrevious(treeEnd);

            currentNode = beforeNode;
        }

        return newNodes;
    }

    public void setNodeAsTreeEnd(@Nullable BlixxNodeImpl node) {
        if (node == null) {
            return;
        }

        BlixxNodeImpl currentNode = this;
        while (currentNode.next != null) {
            currentNode = currentNode.next;
        }

        node.previous = currentNode;
        currentNode.next = node;
    }

    public BlixxNodeImpl findTreeEnd() {
        BlixxNodeImpl currentNode = this;
        while (currentNode.next != null) {
            currentNode = currentNode.next;
        }

        return currentNode;
    }

    public void addOrReplaceTag(BlixxTag.WithDefinedData<?> tag) {
        if (this.tags == null) {
            this.tags = new ObjectArray<>(new BlixxTag.WithDefinedData[]{tag});
            return;
        }

        this.tags = this.tags.filter(tag::canCoexist);
        this.tags.add(tag);
    }

    public TextComponent build(Style defaultStyle) {
        final BlixxProcessor.Component.ComponentContext context = BlixxProcessor.Component.ComponentContext.builder()
                .blixx(this.spec == null ? Blixx.standard() : this.spec.getBlixx())
                .build();

        final FastComponentBuilder rootBuilder = new FastComponentBuilder();
        rootBuilder.setContent("");
        rootBuilder.setStyle(defaultStyle);

        final Iterator<BlixxNodeImpl> iterator = this.iterator(true);

        while (iterator.hasNext()) {
            final BlixxNodeImpl node = iterator.next();
            //            if (node.adventureComponent != null) {
            //                rootBuilder.append(node.adventureComponent);
            //                continue;
            //            }

            context.setNode(node);

            final FastComponentBuilder componentBuilder = new FastComponentBuilder();
            componentBuilder.setContent(node.getContent());

            context.setComponentBuilder(componentBuilder);
            context.setStyleBuilder(new StyleBuilder(defaultStyle));

            // Process visitors
            for (final BlixxTag.WithDefinedData<?> tag : node.getTags()) {
                context.setData(tag.getDefinedData());
                context.setTag(tag);

                final BlixxProcessor processor = tag.getProcessor();
                if (processor instanceof BlixxProcessor.Component.Decorator) {
                    ((BlixxProcessor.Component.Decorator) processor).decorate(context);
                }

                if (processor instanceof BlixxProcessor.Component.Visitor) {
                    ((BlixxProcessor.Component.Visitor) processor).visit(context);
                }
            }

            if (node.content.isEmpty()) {
                continue;
            }

            componentBuilder.setStyle(context.getStyleBuilder().build());
            rootBuilder.append(componentBuilder.build());
        }

        return rootBuilder.build();
    }

    protected Component buildAdventure() {
        final BlixxProcessor.Component.ComponentContext context = BlixxProcessor.Component.ComponentContext.builder().blixx(this.spec.getBlixx()).build();
        final FastComponentBuilder componentBuilder = new FastComponentBuilder();
        componentBuilder.setContent(this.content);

        context.setComponentBuilder(componentBuilder);
        context.setStyleBuilder(new StyleBuilder(null));

        for (final BlixxTag.WithDefinedData<?> tag : this.getTags()) {
            final BlixxProcessor processor = tag.getProcessor();
            context.setData(tag.getDefinedData());

            if (processor instanceof BlixxProcessor.Component.Decorator) {
                ((BlixxProcessor.Component.Decorator) processor).decorate(context);
            }
        }

        final Style build = context.getStyleBuilder().build();
        componentBuilder.setStyle(build);

        return componentBuilder.build();
    }

    protected ObjectArray<BlixxTag.WithDefinedData<?>> copyTags() {
        if (!this.hasIndexableTagData) {
            return this.tags == null ? null : new ObjectArray<>(this.tags);
        }

        return this.tags.map(this::copyTag);
    }

    // Helper method assumed for the loop check (basic implementation)
    private boolean containsNode(BlixxNodeImpl structure, BlixxNodeImpl nodeToCheck) {
        BlixxNodeImpl current = structure;
        while (current != null) {
            if (current == nodeToCheck) {
                return true;
            }
            current = current.getNext(); // Simple linear check
        }
        return false;
    }

    private void inherit(BlixxNodeImpl from) {
        this.content = from.content;
        this.tags = from.tags;
        this.adventureComponent = from.adventureComponent;
        this.hasIndexableTagData = from.hasIndexableTagData;
        this.next = from.next;
    }

    @SuppressWarnings("unchecked")
    private <T> BlixxTag.WithDefinedData<T> copyTag(BlixxTag.WithDefinedData<T> tag) {
        if (!(tag.getDefinedData() instanceof Indexable)) {
            return tag;
        }

        return new TagWithDefinedDataImpl<>(tag, (T) ((Indexable) tag.getDefinedData()).copy());
    }
}
