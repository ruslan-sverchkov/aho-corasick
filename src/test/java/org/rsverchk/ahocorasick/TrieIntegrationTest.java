package org.rsverchk.ahocorasick;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Integration tests for Aho-Coraskick.
 *
 * @author Ruslan Sverchkov
 */
public class TrieIntegrationTest {

    @Test
    public void test() {
        TrieBuilder<String> builder = new TrieBuilder<>();
        Trie<String> trie = builder.addCharSequence("hers", "hers")
                .addCharSequence("his", "his")
                .addCharSequence("sher", "sher")
                .addCharSequence("he", "he")
                .build();

        Set<Triple<Integer, Integer, String>> set = new HashSet<>();
        trie.match("shers", (int beginIndex, int endIndex, String payload) -> {
            set.add(new ImmutableTriple<>(beginIndex, endIndex, payload));
            return true;
        });

        assertEquals(ImmutableSet.of(new ImmutableTriple<>(0, 4, "sher"),
                new ImmutableTriple<>(1, 3, "he"),
                new ImmutableTriple<>(1, 5, "hers")),
                set);
    }

}