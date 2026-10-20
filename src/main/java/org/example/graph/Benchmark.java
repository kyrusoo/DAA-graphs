package org.example.graph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public final class Benchmark {
    private static final int[] SIZES = {1_000, 3_162, 10_000, 31_623, 100_000};
    private static final int[] DENSITIES = {4, 20};
    private static final int RUNS = 5;
    private static final Reference[][] REFERENCES = {
            {new Reference(147_970, 27, 396_274), new Reference(30_363, 1, 252_411)},
            {new Reference(468_911, 137, 1_271_911), new Reference(97_589, 1, 723_157)},
            {new Reference(1_486_139, 373, 3_941_828), new Reference(309_550, 1, 2_381_342)},
            {new Reference(4_715_515, 1_188, 12_551_891), new Reference(968_517, 1, 7_376_612)},
            {new Reference(15_012_094, 3_923, 40_096_539), new Reference(3_056_681, 1, 23_512_087)}
    };

    private Benchmark() { }

    public static void run(Path outputDirectory) throws IOException {
        List<CsvRow> rows = new ArrayList<>(50);
        for (int sizeIndex = 0; sizeIndex < SIZES.length; sizeIndex++) {
            int n = SIZES[sizeIndex];
            for (int densityIndex = 0; densityIndex < DENSITIES.length; densityIndex++) {
                int m = n * DENSITIES[densityIndex];
                Reference reference = REFERENCES[sizeIndex][densityIndex];
                String density = densityIndex == 0 ? "sparse" : "dense";
                long mst = benchmarkMst(n, m, density, reference, rows);
                int scc = benchmarkScc(n, m, density, reference, rows);
                long distances = benchmarkDag(n, m, density, reference, rows);
                System.out.printf("n=%d m=%d MST=%d SCC=%d DAG=%d%n", n, m,
                        mst, scc, distances);
            }
        }
        Files.createDirectories(outputDirectory.resolve("plots"));
        writeCsv(outputDirectory.resolve("results.csv"), rows);
        Plotter.write(rows, outputDirectory.resolve("plots"));
    }

    private static long benchmarkMst(int n, int m, String density, Reference reference, List<CsvRow> rows) {
        Graph graph = GraphGenerator.undirected(n, m);
        Measured<MstResult> kruskal = measure(metrics -> MstAlgorithms.kruskal(graph, metrics),
                result -> Long.toString(result.totalWeight()));
        Measured<MstResult> prim = measure(metrics -> MstAlgorithms.prim(graph, metrics),
                result -> Long.toString(result.totalWeight()));
        if (kruskal.result.totalWeight() != prim.result.totalWeight())
            throw new AssertionError("Kruskal and Prim disagree at n=" + n + ", m=" + m);
        if (kruskal.result.totalWeight() != reference.mstWeight ||
                kruskal.result.components() != 1 || prim.result.components() != 1)
            throw new AssertionError("MST reference mismatch at n=" + n + ", m=" + m);
        rows.add(row("Kruskal", "undirected_" + density, n, m, kruskal));
        rows.add(row("Prim", "undirected_" + density, n, m, prim));
        return kruskal.result.totalWeight();
    }

    private static int benchmarkScc(int n, int m, String density, Reference reference, List<CsvRow> rows) {
        Graph graph = GraphGenerator.directed(n, m);
        Measured<SccResult> kosaraju = measure(metrics -> Kosaraju.find(graph, metrics),
                result -> Integer.toString(result.components().size()));
        if (kosaraju.result.components().size() != reference.sccCount)
            throw new AssertionError("SCC reference mismatch at n=" + n + ", m=" + m +
                    ": " + kosaraju.result.components().size());
        if (kosaraju.metrics.edgeScans != 2L * m) throw new AssertionError("Kosaraju must scan 2m edges");
        rows.add(row("Kosaraju", "directed_" + density, n, m, kosaraju));
        return kosaraju.result.components().size();
    }

    private static long benchmarkDag(int n, int m, String density, Reference reference, List<CsvRow> rows) {
        Graph graph = GraphGenerator.dag(n, m);
        Measured<TopologicalResult> kahn = measure(metrics -> TopologicalSort.kahn(graph, metrics),
                result -> Integer.toString(result.order().size()));
        if (kahn.result.hasCycle() || kahn.result.order().size() != n)
            throw new AssertionError("Kahn failed to order generated DAG");
        validateOrder(graph, kahn.result.order());
        if (kahn.metrics.edgeScans != m) throw new AssertionError("Kahn must scan m edges");
        rows.add(row("Kahn", "dag_" + density, n, m, kahn));

        Measured<ShortestPathResult> shortest = measure(
                metrics -> DagShortestPaths.solve(graph, kahn.result, metrics),
                result -> Long.toString(result.sumDistances()));
        if (shortest.result.sumDistances() != reference.dagDistanceSum)
            throw new AssertionError("DAG distance reference mismatch at n=" + n + ", m=" + m +
                    ": " + shortest.result.sumDistances());
        if (shortest.metrics.edgeScans != m) throw new AssertionError("DAG relaxation must scan m edges");
        rows.add(row("DagShortestPath", "dag_" + density, n, m, shortest));
        return shortest.result.sumDistances();
    }

    private static <T> Measured<T> measure(Function<Metrics, T> algorithm, Function<T, String> resultText) {
        double[] elapsed = new double[RUNS];
        Metrics firstMetrics = null;
        T firstResult = null;
        String firstText = null;
        for (int run = 0; run < RUNS; run++) {
            Metrics metrics = new Metrics();
            long start = System.nanoTime();
            T result = algorithm.apply(metrics);
            elapsed[run] = (System.nanoTime() - start) / 1_000_000.0;
            String text = resultText.apply(result);
            if (run == 0) {
                firstResult = result;
                firstText = text;
                firstMetrics = metrics;
            } else if (!firstText.equals(text) || !sameCounters(firstMetrics, metrics)) {
                throw new AssertionError("algorithm produced non-reproducible results or counters");
            }
        }
        Arrays.sort(elapsed);
        firstMetrics.timeMs = elapsed[RUNS / 2];
        return new Measured<>(firstResult, firstMetrics);
    }

    private static boolean sameCounters(Metrics a, Metrics b) {
        return a.dsuOps == b.dsuOps && a.edgeScans == b.edgeScans &&
                a.comparisons == b.comparisons && a.maxDepth == b.maxDepth;
    }

    private static CsvRow row(String algorithm, String graph, int n, int m, Measured<?> measured) {
        Metrics x = measured.metrics;
        long result = switch (algorithm) {
            case "Kruskal", "Prim" -> ((MstResult) measured.result).totalWeight();
            case "Kosaraju" -> ((SccResult) measured.result).components().size();
            case "Kahn" -> ((TopologicalResult) measured.result).order().size();
            case "DagShortestPath" -> ((ShortestPathResult) measured.result).sumDistances();
            default -> throw new IllegalArgumentException("unknown algorithm " + algorithm);
        };
        return new CsvRow(algorithm, graph, n, m, x.timeMs, x.dsuOps, x.edgeScans,
                x.comparisons, x.maxDepth, result);
    }

    private static void validateOrder(Graph graph, List<Integer> order) {
        int[] position = new int[graph.vertexCount()];
        for (int i = 0; i < order.size(); i++) position[order.get(i)] = i;
        for (Edge edge : graph.edges()) if (position[edge.from()] >= position[edge.to()])
            throw new AssertionError("invalid topological order for edge " + edge);
    }

    private static void writeCsv(Path path, List<CsvRow> rows) throws IOException {
        List<String> lines = new ArrayList<>(rows.size() + 1);
        lines.add("algorithm,graph,n,m,time_ms,dsu_ops,edge_scans,comparisons,max_depth,result");
        for (CsvRow row : rows) lines.add(row.toCsv());
        Files.write(path, lines);
    }

    private record Reference(long mstWeight, int sccCount, long dagDistanceSum) { }
    private record Measured<T>(T result, Metrics metrics) { }
}
