package org.rsverchk.ahocorasick;

import gnu.trove.list.TCharList;
import gnu.trove.list.array.TCharArrayList;
import gnu.trove.map.hash.TCharObjectHashMap;
import gnu.trove.procedure.TCharObjectProcedure;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Trie node.
 *
 * @param <T> payload type
 * @author Ruslan Sverchkov
 */
public class Node<T> {

    private final Node<T> parent;
    private final int level;

    private TCharObjectHashMap<Node<T>> children;
    private Node<T> suffix;
    private T payload;
    private Node<T> terminalSuffix;

    /**
     * Create a root node.
     *
     * @param <T> payload type
     * @return a root node
     */
    public static <T> Node<T> root() {
        return new Node<>(null, 0);
    }

    /**
     * Check if the node is root
     *
     * @return whether the node is root
     */
    public boolean isRoot() {
        return parent == null;
    }

    /**
     * Check if the node is terminal
     *
     * @return whether the node is terminal
     */
    public boolean isTerminal() {
        return payload != null;
    }

    /**
     * Get parent node.
     *
     * @return parent node
     * @throws IllegalStateException if current node is root, the operation makes no sense for root
     */
    @Nonnull
    public Node<T> getParent() {
        if (isRoot()) {
            throw new IllegalStateException("makes no sense for root");
        }
        return parent;
    }

    /**
     * Get node level (length of the shortest path between the node and root).
     *
     * @return node level
     */
    public int getLevel() {
        return level;
    }

    /**
     * Get suffix.
     *
     * @return suffix
     * @throws IllegalArgumentException if suffix is null, it means that the method is called before the trie is
     *                                  initialized, which makes no sense
     */
    @Nonnull
    public Node<T> getSuffix() {
        if (suffix == null) {
            throw new IllegalStateException("suffix is not set, call setSuffix() first");
        }
        return suffix;
    }

    /**
     * Set suffix.
     *
     * @param suffix suffix to set
     * @throws NullPointerException if suffix is null
     */
    public void setSuffix(@Nonnull Node<T> suffix) {
        Validate.notNull(suffix);
        this.suffix = suffix;
    }

    /**
     * Get terminal suffix.
     *
     * @return terminal suffix, can be null
     */
    @Nullable
    public Node<T> getTerminalSuffix() {
        return terminalSuffix;
    }

    /**
     * Set terminal suffix.
     *
     * @param terminalSuffix terminal suffix
     * @throws NullPointerException if terminalSuffix is null
     */
    public void setTerminalSuffix(@Nonnull Node<T> terminalSuffix) {
        Validate.notNull(terminalSuffix);
        this.terminalSuffix = terminalSuffix;
    }

    /**
     * Get payload.
     *
     * @return payload, can be null if the node is not terminal
     */
    @Nullable
    public T getPayload() {
        return payload;
    }

    /**
     * Set payload.
     *
     * @param payload payload to set
     * @throws NullPointerException if payload is null
     */
    public void setPayload(@Nonnull T payload) {
        Validate.notNull(payload);
        this.payload = payload;
    }

    /**
     * Compact children map.
     */
    public void compact() {
        if (children != null) {
            children.compact();
        }
    }

    /**
     * Create a child for the node.
     *
     * @param key a character corresponding to the child
     * @return the child
     * @throws IllegalArgumentException if the node already has a child corresponding to the specified character
     */
    public Node<T> createChild(char key) {
        if (children != null) {
            Validate.isTrue(!children.containsKey(key), MessageFormat.format("child [{0}] already exists",
                    key));
        } else {
            children = new TCharObjectHashMap<>(1);
        }
        Node<T> child = new Node<>(this, level + 1);
        children.put(key, child);
        return child;
    }

    /**
     * Get a child corresponding to the specified character.
     *
     * @param key character
     * @return a child corresponding to the specified character if exists, null otherwise
     */
    @Nullable
    public Node<T> getChild(char key) {
        return children == null ? null : children.get(key);
    }

    /**
     * Call the specified procedure for each child of current node.
     *
     * @param procedure procedure to call
     * @throws NullPointerException if procedure is null
     */
    public void forEachChild(@Nonnull TCharObjectProcedure<? super Node<T>> procedure) {
        Validate.notNull(procedure);
        if (children == null) {
            return;
        }
        children.forEachEntry(procedure);
    }

    @Override
    public String toString() {
        TCharList list = new TCharArrayList(1);
        Node<T> current = this;
        while (!current.isRoot()) {
            list.add(current.getKey());
            current = current.getParent();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Node {root}");
        for (int i = 0; i < list.size(); i++) {
            sb.append("\n");
            int level = i + 1;
            for (int k = 0; k < level; k++) {
                sb.append("   ");
            }
            char c = list.get(i);
            sb.append("Node {key: [")
                    .append(c)
                    .append("], level: [")
                    .append(level)
                    .append("]}");
        }
        return sb.toString();
    }

    /**
     * Get a key corresponding to current node. Useful for logging.
     *
     * @return a key corresponding to current node
     * @throws IllegalStateException if current node is root, the operation makes no sense for root
     */
    protected char getKey() {
        if (isRoot()) {
            throw new IllegalStateException("makes no sense for root");
        }
        return parent.getChildKey(this);
    }

    /**
     * Find a character corresponding to the specified child. The character is useful for logging but it's not stored
     * as a node field for memory efficiency's sake, so the only way to determine it is to use parent's children map.
     *
     * @param child the child to find a character for
     * @return a character corresponding to the specified child
     * @throws NullPointerException     if child is null
     * @throws IllegalArgumentException if the specified node is not a child of current node
     */
    protected char getChildKey(@Nonnull Node<T> child) {
        Validate.notNull(child);
        AtomicReference<Character> ref = new AtomicReference<>();
        forEachChild((char key, Node<T> node) -> {
            if (node.equals(child)) {
                ref.set(key);
                return false;
            }
            return true;
        });
        if (ref.get() == null) {
            throw new IllegalArgumentException("no such child");
        }
        return ref.get();
    }

    /**
     * Get children. For testing purposes.
     *
     * @return children
     */
    @Nullable
    protected TCharObjectHashMap<Node<T>> getChildren() {
        return children;
    }

    /**
     * Set children. For testing purposes.
     *
     * @param children children to set
     * @throws NullPointerException if children is null
     */
    public void setChildren(@Nonnull TCharObjectHashMap<Node<T>> children) {
        Validate.notNull(children);
        this.children = children;
    }

    /**
     * Create an instance of Node. If parent is null and level is 0, the node is considered to be a root node.
     * If parent is not null and level is > 0, the node is considered to be a simple node.
     *
     * @param parent node parent
     * @param level  node level
     * @throws IllegalArgumentException if:
     *                                  * parent is null and level is not 0
     *                                  * parent is not null and level is not > 0
     */
    protected Node(@Nullable Node<T> parent, int level) {
        if (parent == null) {
            Validate.isTrue(level == 0);
        } else {
            Validate.isTrue(level > 0);
        }
        this.parent = parent;
        this.level = level;
    }

}