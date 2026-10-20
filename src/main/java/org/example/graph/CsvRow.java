package org.example.graph;

public record CsvRow(String algorithm, String graph, int n, int m, double timeMs,
                     long dsuOps, long edgeScans, long comparisons, int maxDepth, long result) {
    public String toCsv() {
        return String.format(java.util.Locale.ROOT, "%s,%s,%d,%d,%.3f,%d,%d,%d,%d,%d",
                algorithm, graph, n, m, timeMs, dsuOps, edgeScans, comparisons, maxDepth, result);
    }
}
