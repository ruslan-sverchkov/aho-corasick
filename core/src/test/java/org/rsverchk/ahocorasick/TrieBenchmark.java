package org.rsverchk.ahocorasick;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Benchmarks for Aho-Corasick.
 *
 * @author Ruslan Sverchkov
 */
public class TrieBenchmark {

    @State(Scope.Benchmark)
    public static class MyState {

        public Trie<String> trie;
        public List<String> lines;
        public String textOf10Symbols;
        public String textOf100Symbols;
        public String textOf1000Symbols;
        public String textOf10000Symbols;
        public String textOf50000Symbols;

        @Setup(Level.Trial)
        public void setup() throws Exception {
            URL url = Thread.currentThread().getContextClassLoader().getResource("google-10000-english.txt");
            lines = Files.readAllLines(Paths.get(url.toURI()));
            TrieBuilder<String> builder = new TrieBuilder<>();
            for (String s : lines) {
                builder.addCharSequence(s, s);
            }
            trie = builder.build();
            String text = StringUtils.join(lines, " ");
            textOf10Symbols = text.substring(0, 10);
            textOf100Symbols = text.substring(0, 100);
            textOf1000Symbols = text.substring(0, 1000);
            textOf10000Symbols = text.substring(0, 10000);
            textOf50000Symbols = text.substring(0, 50000);
        }

    }

    public static void main(String ... args) throws IOException, RunnerException {
        Main.main(args);
    }

    @Benchmark
    @Fork(value = 1, warmups = 0)
    @Measurement(iterations = 10)
    @Warmup(iterations = 10)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void testTrie_TextOf10Symbols(MyState state, Blackhole blackhole) {
        testTrie(state.trie, state.textOf10Symbols, blackhole);
    }

    @Benchmark
    @Fork(value = 1, warmups = 0)
    @Measurement(iterations = 10)
    @Warmup(iterations = 10)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void testTrie_TextOf100Symbols(MyState state, Blackhole blackhole) {
        testTrie(state.trie, state.textOf100Symbols, blackhole);
    }

    @Benchmark
    @Fork(value = 1, warmups = 0)
    @Measurement(iterations = 10)
    @Warmup(iterations = 10)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void testTrie_TextOf1000Symbols(MyState state, Blackhole blackhole) {
        testTrie(state.trie, state.textOf1000Symbols, blackhole);
    }

    @Benchmark
    @Fork(value = 1, warmups = 0)
    @Measurement(iterations = 10)
    @Warmup(iterations = 10)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void testTrie_TextOf10000Symbols(MyState state, Blackhole blackhole) {
        testTrie(state.trie, state.textOf10000Symbols, blackhole);
    }

    @Benchmark
    @Fork(value = 1, warmups = 0)
    @Measurement(iterations = 10)
    @Warmup(iterations = 10)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void testTrie_TextOf50000Symbols(MyState state, Blackhole blackhole) {
        testTrie(state.trie, state.textOf50000Symbols, blackhole);
    }

    @Benchmark
    @Fork(value = 1, warmups = 0)
    @Measurement(iterations = 10)
    @Warmup(iterations = 10)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void testBruteForce_TextOf10Symbols(MyState state, Blackhole blackhole) {
        testBruteForce(state.lines, state.textOf10Symbols, blackhole);
    }

    @Benchmark
    @Fork(value = 1, warmups = 0)
    @Measurement(iterations = 10)
    @Warmup(iterations = 10)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void testBruteForce_TextOf100Symbols(MyState state, Blackhole blackhole) {
        testBruteForce(state.lines, state.textOf100Symbols, blackhole);
    }

    @Benchmark
    @Fork(value = 1, warmups = 0)
    @Measurement(iterations = 10)
    @Warmup(iterations = 10)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void testBruteForce_TextOf1000Symbols(MyState state, Blackhole blackhole) {
        testBruteForce(state.lines, state.textOf1000Symbols, blackhole);
    }

    @Benchmark
    @Fork(value = 1, warmups = 0)
    @Measurement(iterations = 10)
    @Warmup(iterations = 10)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void testBruteForce_TextOf10000Symbols(MyState state, Blackhole blackhole) {
        testBruteForce(state.lines, state.textOf10000Symbols, blackhole);
    }

    @Benchmark
    @Fork(value = 1, warmups = 0)
    @Measurement(iterations = 10)
    @Warmup(iterations = 10)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void testBruteForce_TextOf50000Symbols(MyState state, Blackhole blackhole) {
        testBruteForce(state.lines, state.textOf50000Symbols, blackhole);
    }

    private void testTrie(Trie<String> trie, String text, Blackhole blackhole) {
        AtomicLong counter = new AtomicLong();
        trie.match(text, (int beginIndex, int endIndex, String payload) -> {
            counter.incrementAndGet();
            return true;
        });
        blackhole.consume(counter);
    }

    private void testBruteForce(List<String> lines, String text, Blackhole blackhole) {
        for (String s : lines) {
            int index = text.indexOf(s);
            while (index != -1) {
                blackhole.consume(index);
                blackhole.consume(index + s.length());
                index = text.indexOf(s, index + 1);
            }
        }
    }

}