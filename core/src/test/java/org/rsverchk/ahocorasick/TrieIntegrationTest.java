package org.rsverchk.ahocorasick;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Test;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.*;
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

    @Test
    public void testBigTrie() throws IOException, URISyntaxException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("google-10000-english.txt");
        List<String> lines = Files.readAllLines(Paths.get(url.toURI()));
        TrieBuilder<String> builder = new TrieBuilder<>();
        for (String s : lines) {
            builder.addCharSequence(s, s);
        }
        Trie<String> trie = builder.build();

        String text = StringUtils.join(lines, " ");

        // todo test uses code that itself needs to be tested - very bad, do something with it when inspiration comes in
        Set<Triple<Integer, Integer, String>> expectedMatches = new HashSet<>();
        for (String s : lines) {
            int index = text.indexOf(s);
            while (index != -1) {
                expectedMatches.add(Triple.of(index, index + s.length(), s));
                index = text.indexOf(s, index + 1);
            }
        }

        Set<Triple<Integer, Integer, String>> matches = new HashSet<>();
        trie.match(text, (int beginIndex, int endIndex, String payload) -> {
            matches.add(Triple.of(beginIndex, endIndex, payload));
            return true;
        });

        assertThat(matches, equalTo(expectedMatches));
    }

}