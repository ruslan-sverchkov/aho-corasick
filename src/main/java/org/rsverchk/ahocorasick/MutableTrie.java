package org.rsverchk.ahocorasick;

import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
        root.setSuffix(root);
        breadthFirstTraversal(this::initNode);
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
                if (!handleMatch(node, index, handler)) {
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
     * Find suffix node for the specified node. Pay attention that by the time we look for suffix of the node on level
     * N, all suffixes of all nodes on levels from 1 to N-1 must be set. This can be ensured by breadth-first traversal
     * of the trie.
     *
     * @param key  a key corresponding to node
     * @param node the node to find suffix for
     * @return suffix node for the specified node
     * @throws NullPointerException  if node is null
     * @throws IllegalStateException if the method is called before all suffixes of all nodes on higher levels are set
     */
    @Nonnull
    protected Node<T> findSuffix(char key, @Nonnull Node<T> node) {
        Validate.notNull(node);
        Node<T> parent = node.getParent();
        if (parent.isRoot()) {
            return parent; // for direct descendants of the root it is the suffix
        }
        Node<T> parentSuffix = parent.getSuffix(); // get parent suffix
        Node<T> suffixChild = parentSuffix.getChild(key); // get suffix child corresponding to the key
        while (suffixChild == null) { // if no such child
            if (parentSuffix.isRoot()) { // and we're in the root
                return parentSuffix; // return root
            }
            parentSuffix = parentSuffix.getSuffix(); // if we're not in the root, get current node's suffix
            suffixChild = parentSuffix.getChild(key); // get suffix child corresponding to the key
        }
        return suffixChild; // a suitable node is found, return it
    }

    /**
     * Find the nearest terminal suffix for the specified node. Pay attention that by the time we look for a terminal
     * suffix, all suffixes of all nodes on levels from 1 to N-1 must be set must be set. This can be ensured by
     * breadth-first traversal of the trie.
     *
     * @return the nearest terminal suffix for the specified node, null if there are no candidates
     * @throws NullPointerException if node is null
     */
    @Nullable
    protected Node<T> findTerminalSuffix(@Nonnull Node<T> node) {
        Validate.notNull(node);
        Node<T> currentSuffix = node.getSuffix();
        while (true) {
            if (currentSuffix.isRoot()) {
                return null;
            }
            if (currentSuffix.isTerminal()) {
                return currentSuffix;
            }
            Node<T> current = currentSuffix;
            currentSuffix = current.getSuffix();
        }
    }

    /**
     * Set suffix, terminal suffix and compact the node.
     *
     * @param c    character corresponding to the node
     * @param node th node to init
     */
    protected void initNode(char c, @Nonnull Node<T> node) {
        Validate.notNull(node);
        Node<T> suffix = findSuffix(c, node);
        node.setSuffix(suffix);
        Node<T> terminalSuffix = findTerminalSuffix(node);
        if (terminalSuffix != null) {
            node.setTerminalSuffix(terminalSuffix);
        }
        node.compact();
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
     * A position corresponding to the current node has been found in an input string, traverse the trie starting from
     * the current node and call the specified handler for each terminal node that will be encountered.
     *
     * @param index   a position in an input string corresponding to the current node
     * @param handler a match handler
     * @return whether to continue matching or not
     * @throws NullPointerException     if handler is null
     * @throws IllegalArgumentException if index + 1 is lesser than the node level (it means that the match has been
     *                                  found in a substring that is shorter than the match itself which obviously
     *                                  cannot happen without programming errors)
     */
    protected boolean handleMatch(@Nonnull Node<T> node, int index, @Nonnull MatchHandler<T> handler) {
        int endOfWordExclusive = index + 1;
        Validate.isTrue(endOfWordExclusive >= node.getLevel());
        Validate.notNull(handler);
        Node<T> current = node;
        while (current != null) {
            if (current.isTerminal()) {
                if (!handler.handle(endOfWordExclusive - current.getLevel(), endOfWordExclusive,
                        current.getPayload())) {
                    return false;
                }
            }
            current = current.getTerminalSuffix();
        }
        return true;
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