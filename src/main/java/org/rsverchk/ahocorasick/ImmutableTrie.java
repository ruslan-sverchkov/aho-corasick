package org.rsverchk.ahocorasick;

import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Trie adapter that ensures thread-safety through immutability. Could just use the interface instead but someone may
 * decide to use instanceof for some reason. This is not a panacea but better than nothing.
 *
 * @param <T> payload type
 * @author Ruslan Sverchkov
 */
@ThreadSafe
public class ImmutableTrie<T> implements Trie<T> {

    private final Trie<T> trie;

    /**
     * Construct an instance of ImmutableTrie.
     *
     * @param trie trie to wrap
     * @throws NullPointerException if trie is null
     */
    public ImmutableTrie(@Nonnull Trie<T> trie) {
        Validate.notNull(trie);
        this.trie = trie;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void match(@Nonnull CharSequence sequence, @Nonnull MatchHandler<T> handler) {
        trie.match(sequence, handler);
    }

}