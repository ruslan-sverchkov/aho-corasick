package org.rsverchk.ahocorasick;

import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Aho-Corasick implementation. Payload can be associated with any added characters sequence so that clients can store
 * useful information corresponding to the sequence. The class is mutable by nature so valid usage of methods is
 * important:
 * 1) add as much characters sequences as you need to the trie using
 * {@link MutableTrie#addCharSequence(CharSequence, Object)} method
 * 2) initialize the trie using {@link MutableTrie#init()} method. When the trie is initialized, no more information
 * can be added to it since it would get it corrupted.
 * 3) now you can perform matching using {@link MutableTrie#match(CharSequence, MatchHandler)} method
 * Illegal sequence of calls will lead to IllegalStateException.
 *
 * @param <T> payload type
 * @author Ruslan Sverchkov
 */
@NotThreadSafe
public class MutableTrie<T> implements Trie<T> {

    private final CharConverter converter;
    private Node<T> root;
    private boolean built;

    /**
     * Construct an instance of MutableTrie.
     *
     * @param converter a function used to transform characters before adding/searching, for example toLowerCase
     * @throws NullPointerException if converter is null
     */
    public MutableTrie(@Nonnull CharConverter converter) {
        Validate.notNull(converter);
        this.converter = converter;
        root = Node.root();
        built = false;
    }

    /**
     * Add the specified characters sequence to the trie.
     *
     * @param sequence a characters sequence to add
     * @param payload  payload associated with the sequence, can be any useful information
     * @throws NullPointerException     if any of the arguments is null
     * @throws IllegalArgumentException if sequence is empty
     * @throws IllegalStateException    if called on already initialized trie
     */
    public void addCharSequence(@Nonnull CharSequence sequence, @Nonnull T payload) {
        Validate.notEmpty(sequence);
        Validate.notNull(payload);
        if (built) {
            throw new IllegalStateException("cannot modify an initialized trie");
        }
        Node<T> current = root;
        for (int i = 0; i < sequence.length(); i++) {
            char character = sequence.charAt(i);
            char converted = converter.convert(character);
            Node<T> next = current.getChild(converted);
            if (next == null) {
                next = current.createChild(converted);
            }
            current = next;
        }
        current.setPayload(payload);
    }

    /**
     * Set suffix, terminal suffix and compact all trie nodes.
     *
     * @throws IllegalStateException if called on already initialized trie
     */
    public void init() {
        if (built) {
            throw new IllegalStateException("cannot modify an initialized trie");
        }
        root.init();
        breadthFirstTraversal(getNodeInitializer());
        built = true;
    }

    /**
     * Match the specified characters sequence against the trie. Call the specified handler when a match is found.
     *
     * @param sequence a characters sequence to look for matches in
     * @param handler  a handler to call when a match is found
     * @throws NullPointerException     if any of the arguments is null
     * @throws IllegalArgumentException if sequence is empty
     * @throws IllegalStateException    if the called on not initialized trie
     */
    @Override
    public void match(@Nonnull CharSequence sequence, @Nonnull MatchHandler<T> handler) {
        Validate.notEmpty(sequence);
        Validate.notNull(handler);
        if (!built) {
            throw new IllegalStateException("call build() first");
        }
        Node<T> current = root;
        int index = 0;
        while (index < sequence.length()) {
            char character = sequence.charAt(index);
            char converted = converter.convert(character);
            Node<T> node = current.getChild(converted);
            if (node != null) {
                if (!node.handleMatch(index, handler)) {
                    return;
                }
                current = node;
                index++;
            } else {
                if (current.isRoot()) {
                    index++;
                } else {
                    current = current.getSuffix();
                }
            }
        }
    }

    /**
     * Perform breadth-first traversal of the trie, call the specified consumer for each node except for root.
     *
     * @param consumer nodes consumer
     * @throws NullPointerException if consumer is null
     */
    protected void breadthFirstTraversal(@Nonnull NodeConsumer<T> consumer) {
        Validate.notNull(consumer);
        Queue<Node<T>> queue = new LinkedList<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            Node<T> parent = queue.remove();
            parent.forEachChild((char key, Node<T> value) -> {
                consumer.consume(key, value);
                queue.add(value);
                return true;
            });
        }
    }

    /**
     * Get a node initialization function. Mostly for testing purposes.
     *
     * @return a node initialization function
     */
    @Nonnull
    protected NodeConsumer<T> getNodeInitializer() {
        return (c, node) -> node.init();
    }

    /*
    Implementation comment:
    The methods are useful for testing purposes. Even though the class is very simple, it's still a full-fledged state
    machine and for white box testing of a state machine we need an ability to set its state.
     */

    protected void setRoot(@Nonnull Node<T> root) {
        Validate.notNull(root);
        this.root = root;
    }

    @Nonnull
    public Node<T> getRoot() {
        return root;
    }

    protected void setBuilt(boolean built) {
        this.built = built;
    }

    public boolean isBuilt() {
        return built;
    }

}