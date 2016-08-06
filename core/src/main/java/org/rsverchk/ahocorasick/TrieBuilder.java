package org.rsverchk.ahocorasick;

import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * Trie builder.
 *
 * @param <T> payload type
 * @author Ruslan Sverchkov
 */
@NotThreadSafe
public class TrieBuilder<T> {

    private MutableTrie<T> trie;

    /**
     * Specify custom characters converter for trie.
     *
     * @param converter characters converter
     * @return this
     * @throws NullPointerException if converter is null
     */
    @Nonnull
    public TrieBuilder<T> withConverter(@Nonnull CharConverter converter) {
        Validate.notNull(converter);
        if (trie != null) {
            throw new IllegalStateException("must be called before addCharSequence()");
        }
        trie = createMutableTrie(converter);
        return this;
    }

    /**
     * Make trie case insensitive.
     *
     * @return this
     */
    @Nonnull
    public TrieBuilder<T> ignoreCase() {
        if (trie != null) {
            throw new IllegalStateException("must be called before addCharSequence()");
        }
        trie = createMutableTrie(createToLowerCaseConverter());
        return this;
    }

    /**
     * Add the specified characters sequence to the trie.
     *
     * @param sequence a characters sequence to add
     * @param payload  payload associated with the sequence, can be any useful information
     * @throws NullPointerException     if any of the arguments is null
     * @throws IllegalArgumentException if sequence is empty
     * @throws IllegalStateException    if called after build()
     */
    @Nonnull
    public TrieBuilder<T> addCharSequence(@Nonnull CharSequence sequence, @Nonnull T payload) {
        if (trie == null) {
            trie = createMutableTrie(createEmptyConverter());
        }
        trie.addCharSequence(sequence, payload);
        return this;
    }

    /**
     * Create and initialize an instance of a trie.
     *
     * @return initialized instance of a trie
     */
    @Nonnull
    public Trie<T> build() {
        if (trie == null) {
            trie = createMutableTrie(createEmptyConverter());
        }
        trie.init();
        return createImmutableTrie(trie);
    }

    /**
     * Create an instance of case insensitive converter. Mostly for testing purposes.
     *
     * @return an instance of case insensitive converter
     */
    @Nonnull
    protected CharConverter createToLowerCaseConverter() {
        return Character::toLowerCase;
    }

    /**
     * Create an instance of converter that doesn't change character. Mostly for testing purposes.
     *
     * @return an instance of converter that doesn't change character
     */
    @Nonnull
    protected CharConverter createEmptyConverter() {
        return c -> c;
    }

    /**
     * Create an instance of {@link MutableTrie}. Mostly for testing purposes.
     *
     * @param converter char converter
     * @return an instance of {@link MutableTrie}
     * @throws NullPointerException if converter is null
     */
    @Nonnull
    protected MutableTrie<T> createMutableTrie(@Nonnull CharConverter converter) {
        return new MutableTrie<>(converter);
    }

    /**
     * Create an instance of immutable trie. Ensures thread-safety through immutability. Could just use the interface
     * instead but someone may decide to use instanceof for some reason. This is not a panacea but better than nothing.
     *
     * @param trie trie to wrap
     * @return an instance of immutable trie
     * @throws NullPointerException if trie is null
     */
    @Nonnull
    protected Trie<T> createImmutableTrie(@Nonnull Trie<T> trie) {
        return trie::match;
    }

    /*
    Implementation comment:
    The methods are useful for testing purposes. Even though the class is very simple, it's still a full-fledged state
    machine and for white box testing of a state machine we need an ability to set its state.
     */

    public void setTrie(@Nullable MutableTrie<T> trie) {
        this.trie = trie;
    }

    @Nullable
    public MutableTrie<T> getTrie() {
        return trie;
    }

}