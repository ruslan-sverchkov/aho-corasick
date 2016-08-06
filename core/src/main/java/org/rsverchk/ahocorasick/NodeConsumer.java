package org.rsverchk.ahocorasick;

import javax.annotation.Nonnull;

/**
 * A function used to process nodes of a trie during traversal. Can be anything you want.
 *
 * @param <T> payload type
 * @author Ruslan Sverchkov
 */
@FunctionalInterface
public interface NodeConsumer<T> {

    /**
     * Process a node of a trie during traversal.
     *
     * @param c    character corresponding to the node
     * @param node node to process
     * @throws NullPointerException if node is null
     */
    void consume(char c, @Nonnull Node<T> node);

}