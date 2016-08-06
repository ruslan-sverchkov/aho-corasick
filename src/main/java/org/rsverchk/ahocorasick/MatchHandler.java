package org.rsverchk.ahocorasick;

import javax.annotation.Nonnull;

/**
 * Handle match of a substring of the input text to trie contents.
 *
 * @param <T> payload type
 * @author Ruslan Sverchkov
 */
@FunctionalInterface
public interface MatchHandler<T> {

    /**
     * Handle match of a substring of the input text to trie contents.
     *
     * @param beginIndex the beginning index of the match in the input text, inclusive.
     * @param endIndex   the ending index of the match in the input text, exclusive.
     * @param payload    teh payload corresponding to the matching node in the trie
     * @return whether to continue matching or not
     * @throws NullPointerException     if payload is null
     * @throws IllegalArgumentException if:
     *                                  * beginIndex is negative
     *                                  * endIndex is less or equal to beginIndex
     */
    boolean handle(int beginIndex, int endIndex, @Nonnull T payload);

}