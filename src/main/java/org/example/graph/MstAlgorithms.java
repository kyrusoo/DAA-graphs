package org.example.graph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public final class MstAlgorithms {
    private MstAlgorithms() { }

    public static MstResult kruskal(Graph graph, Metrics metrics) {
        requireUndirected(graph);
        List<Edge> sorted = new ArrayList<>(graph.edges());
        sorted.sort((a, b) -> compareWeights(a.weight(), b.weight(), metrics,
                a.from(), a.to(), b.from(), b.to()));

        DisjointSetUnion sets = new DisjointSetUnion(graph.vertexCount(), metrics);
        List<Edge> chosen = new ArrayList<>();
        long total = 0;
        for (Edge edge : sorted) {
            metrics.edgeScans++;
            int first = sets.find(edge.from());
            int second = sets.find(edge.to());
            if (first != second) {
                sets.union(edge.from(), edge.to());
                chosen.add(edge);
                total += edge.weight();
                if (chosen.size() == graph.vertexCount() - 1) break;
            }
        }
        return new MstResult(total, sets.components(), chosen);
    }

    public static MstResult prim(Graph graph, Metrics metrics) {
        requireUndirected(graph);
        boolean[] inTree = new boolean[graph.vertexCount()];
        PriorityQueue<Edge> queue = new PriorityQueue<>((a, b) -> compareWeights(a.weight(), b.weight(), metrics,
                a.from(), a.to(), b.from(), b.to()));
        List<Edge> chosen = new ArrayList<>();
        long total = 0;
        int components = 0;

        for (int root = 0; root < graph.vertexCount(); root++) {
            if (inTree[root]) continue;
            components++;
            inTree[root] = true;
            addOutgoing(graph, root, inTree, queue, metrics);
            while (!queue.isEmpty()) {
                Edge edge = queue.remove();
                if (inTree[edge.to()]) continue;
                inTree[edge.to()] = true;
                chosen.add(edge);
                total += edge.weight();
                addOutgoing(graph, edge.to(), inTree, queue, metrics);
            }
        }
        return new MstResult(total, components, chosen);
    }

    private static void addOutgoing(Graph graph, int vertex, boolean[] inTree,
                                    PriorityQueue<Edge> queue, Metrics metrics) {
        for (Edge edge : graph.outgoing(vertex)) {
            metrics.edgeScans++;
            if (!inTree[edge.to()]) queue.add(edge);
        }
    }

    private static int compareWeights(int aWeight, int bWeight, Metrics metrics,
                                      int aFrom, int aTo, int bFrom, int bTo) {
        metrics.comparisons++;
        int result = Integer.compare(aWeight, bWeight);
        if (result != 0) return result;
        result = Integer.compare(aFrom, bFrom);
        if (result != 0) return result;
        return Integer.compare(aTo, bTo);
    }

    private static void requireUndirected(Graph graph) {
        if (graph.isDirected()) throw new IllegalArgumentException("MST requires an undirected graph");
    }
}
