package org.rsverchk.ahocorasick;

import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Test {

    @State(Scope.Benchmark)
    public static class MyState {

        public Trie<String> trie;

        @Setup(Level.Trial)
        public void doSetup() {
            TrieBuilder<String> builder = new TrieBuilder<>();
            trie = builder.addCharSequence("hers", "hers")
                    .addCharSequence("his", "his")
                    .addCharSequence("sher", "sher")
                    .addCharSequence("he", "he")
                    .build();
        }

    }

    @Benchmark
    @Fork(value = 1, warmups = 1)
    @Measurement(iterations = 5)
    @Warmup(iterations = 5)
    @Threads(10)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void testMethod(MyState state, Blackhole blackhole) {
        AtomicLong counter = new AtomicLong();
        state.trie.match("shers", (int beginIndex, int endIndex, String payload) -> {
            counter.incrementAndGet();
            return true;
        });
        blackhole.consume(counter);
    }

    public static void main(String ... args) throws IOException, RunnerException {
        Main.main(args);
    }

}