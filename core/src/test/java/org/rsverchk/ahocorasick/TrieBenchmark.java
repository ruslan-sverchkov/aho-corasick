package org.rsverchk.ahocorasick;

import org.apache.commons.lang3.StringUtils;
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
        public String textOf10Symbols;
        public String textOf100Symbols;
        public String textOf1000Symbols;
        public String textOf10000Symbols;
        public String textOf50000Symbols;

        @Setup(Level.Trial)
        public void setup() throws Exception {
            URL url = Thread.currentThread().getContextClassLoader().getResource("google-10000-english.txt");
            List<String> lines = Files.readAllLines(Paths.get(url.toURI()));
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

    @Benchmark
    @Fork(value = 1, warmups = 0)
    @Measurement(iterations = 5)
    @Warmup(iterations = 5)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void testTextOf10Symbols(MyState state, Blackhole blackhole) {
        AtomicLong counter = new AtomicLong();
        state.trie.match(state.textOf10Symbols, (int beginIndex, int endIndex, String payload) -> {
            counter.incrementAndGet();
            return true;
        });
        blackhole.consume(counter);
    }

    @Benchmark
    @Fork(value = 1, warmups = 0)
    @Measurement(iterations = 5)
    @Warmup(iterations = 5)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void testTextOf100Symbols(MyState state, Blackhole blackhole) {
        AtomicLong counter = new AtomicLong();
        state.trie.match(state.textOf100Symbols, (int beginIndex, int endIndex, String payload) -> {
            counter.incrementAndGet();
            return true;
        });
        blackhole.consume(counter);
    }

    @Benchmark
    @Fork(value = 1, warmups = 0)
    @Measurement(iterations = 5)
    @Warmup(iterations = 5)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void testTextOf1000Symbols(MyState state, Blackhole blackhole) {
        AtomicLong counter = new AtomicLong();
        state.trie.match(state.textOf1000Symbols, (int beginIndex, int endIndex, String payload) -> {
            counter.incrementAndGet();
            return true;
        });
        blackhole.consume(counter);
    }

    @Benchmark
    @Fork(value = 1, warmups = 0)
    @Measurement(iterations = 5)
    @Warmup(iterations = 5)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void testTextOf10000Symbols(MyState state, Blackhole blackhole) {
        AtomicLong counter = new AtomicLong();
        state.trie.match(state.textOf10000Symbols, (int beginIndex, int endIndex, String payload) -> {
            counter.incrementAndGet();
            return true;
        });
        blackhole.consume(counter);
    }

    @Benchmark
    @Fork(value = 1, warmups = 0)
    @Measurement(iterations = 5)
    @Warmup(iterations = 5)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void testTextOf50000Symbols(MyState state, Blackhole blackhole) {
        AtomicLong counter = new AtomicLong();
        state.trie.match(state.textOf50000Symbols, (int beginIndex, int endIndex, String payload) -> {
            counter.incrementAndGet();
            return true;
        });
        blackhole.consume(counter);
    }

    public static void main(String ... args) throws IOException, RunnerException {
        Main.main(args);
    }

}