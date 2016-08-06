package org.rsverchk.ahocorasick;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Integration tests for Aho-Corasick.
 *
 * @author Ruslan Sverchkov
 */
public class TrieIntegrationTest {

    @Test
    public void testNoMatches() {
        TrieBuilder<String> builder = new TrieBuilder<>();
        Trie<String> trie = builder.addCharSequence("text", "text")
                .build();

        Set<Triple<Integer, Integer, String>> set = new HashSet<>();
        trie.match("teext", (int beginIndex, int endIndex, String payload) -> {
            set.add(new ImmutableTriple<>(beginIndex, endIndex, payload));
            return true;
        });

        assertTrue(set.isEmpty());
    }

    @Test
    public void testMatchWholeString() {
        TrieBuilder<String> builder = new TrieBuilder<>();
        Trie<String> trie = builder.addCharSequence("text", "text")
                .build();

        Set<Triple<Integer, Integer, String>> set = new HashSet<>();
        trie.match("text", (int beginIndex, int endIndex, String payload) -> {
            set.add(new ImmutableTriple<>(beginIndex, endIndex, payload));
            return true;
        });

        assertEquals(ImmutableSet.of(new ImmutableTriple<>(0, 4, "text")), set);
    }

    @Test
    public void testMatchBeginningOfTheString() {
        TrieBuilder<String> builder = new TrieBuilder<>();
        Trie<String> trie = builder.addCharSequence("text", "text")
                .build();

        Set<Triple<Integer, Integer, String>> set = new HashSet<>();
        trie.match("text1", (int beginIndex, int endIndex, String payload) -> {
            set.add(new ImmutableTriple<>(beginIndex, endIndex, payload));
            return true;
        });

        assertEquals(ImmutableSet.of(new ImmutableTriple<>(0, 4, "text")), set);
    }

    @Test
    public void testMatchMiddleOfTheString() {
        TrieBuilder<String> builder = new TrieBuilder<>();
        Trie<String> trie = builder.addCharSequence("text", "text")
                .build();

        Set<Triple<Integer, Integer, String>> set = new HashSet<>();
        trie.match("1text1", (int beginIndex, int endIndex, String payload) -> {
            set.add(new ImmutableTriple<>(beginIndex, endIndex, payload));
            return true;
        });

        assertEquals(ImmutableSet.of(new ImmutableTriple<>(1, 5, "text")), set);
    }

    @Test
    public void testMatchEndOfTheString() {
        TrieBuilder<String> builder = new TrieBuilder<>();
        Trie<String> trie = builder.addCharSequence("text", "text")
                .build();

        Set<Triple<Integer, Integer, String>> set = new HashSet<>();
        trie.match("1text", (int beginIndex, int endIndex, String payload) -> {
            set.add(new ImmutableTriple<>(beginIndex, endIndex, payload));
            return true;
        });

        assertEquals(ImmutableSet.of(new ImmutableTriple<>(1, 5, "text")), set);
    }

    @Test
    public void testMatchTwice() {
        TrieBuilder<String> builder = new TrieBuilder<>();
        Trie<String> trie = builder.addCharSequence("text", "text")
                .build();

        Set<Triple<Integer, Integer, String>> set = new HashSet<>();
        trie.match("begin text text end", (int beginIndex, int endIndex, String payload) -> {
            set.add(new ImmutableTriple<>(beginIndex, endIndex, payload));
            return true;
        });

        assertEquals(ImmutableSet.of(new ImmutableTriple<>(6, 10, "text"),
                new ImmutableTriple<>(11, 15, "text")), set);
    }

    @Test
    public void testMatchDifferentWords_NoOverlap() {
        TrieBuilder<String> builder = new TrieBuilder<>();
        Trie<String> trie = builder.addCharSequence("foo", "foo")
                .addCharSequence("bar", "bar")
                .build();

        Set<Triple<Integer, Integer, String>> set = new HashSet<>();
        trie.match("foobar", (int beginIndex, int endIndex, String payload) -> {
            set.add(new ImmutableTriple<>(beginIndex, endIndex, payload));
            return true;
        });

        assertEquals(ImmutableSet.of(new ImmutableTriple<>(0, 3, "foo"),
                new ImmutableTriple<>(3, 6, "bar")),
                set);
    }

    @Test
    public void testMatchDifferentWords_Overlap() {
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