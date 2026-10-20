package org.example.graph;

import java.util.Arrays;

public final class DagShortestPaths {
    private static final long INFINITY = Long.MAX_VALUE / 4;

    private DagShortestPaths() { }

    public static ShortestPathResult solve(Graph graph, TopologicalResult topological, Metrics metrics) {
        if (!graph.isDirected()) throw new IllegalArgumentException("DAG shortest paths require a directed graph");
        int n = graph.vertexCount();
        if (topological.hasCycle() || topological.order().size() != n)
            throw new IllegalArgumentException("shortest paths require an acyclic graph");

        long[] distance = new long[n];
        int[] parent = new int[n];
        Arrays.fill(distance, INFINITY);
        Arrays.fill(parent, -1);
        int[] indegree = topological.initialIndegrees();
        for (int vertex = 0; vertex < n; vertex++) if (indegree[vertex] == 0) distance[vertex] = 0;

        for (int vertex : topological.order()) {
            for (Edge edge : graph.outgoing(vertex)) {
                metrics.edgeScans++;
                long candidate = distance[vertex] + edge.weight();
                metrics.comparisons++;
                if (candidate < distance[edge.to()]) {
                    distance[edge.to()] = candidate;
                    parent[edge.to()] = vertex;
                }
            }
        }
        return new ShortestPathResult(distance, parent);
    }
}
