package org.example.graph;

import java.nio.file.Path;

public final class Main {
    private Main() { }

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && !args[0].equals("benchmark"))
            throw new IllegalArgumentException("usage: Main [benchmark]");
        Benchmark.run(Path.of("results"));
    }
}
