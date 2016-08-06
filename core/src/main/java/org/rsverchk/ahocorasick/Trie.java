package org.rsverchk.ahocorasick;

import javax.annotation.Nonnull;

/**
 * API for Aho-Corasick implementations.
 *
 * @param <T> payload type
 * @author Ruslan Sverchkov
 */
@FunctionalInterface
public interface Trie<T> {

    /**
     * Match the specified characters sequence against the trie. Call the specified handler when a match is found.
     *
     * @param sequence a characters sequence to look for matches in
     * @param handler  a handler to call when a match is found
     * @throws NullPointerException     if any of the arguments is null
     * @throws IllegalArgumentException if sequence is empty
     */
    void match(@Nonnull CharSequence sequence, @Nonnull MatchHandler<T> handler);

}