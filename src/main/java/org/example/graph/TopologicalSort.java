package org.example.graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public final class TopologicalSort {
    private TopologicalSort() { }

    public static TopologicalResult kahn(Graph graph, Metrics metrics) {
        if (!graph.isDirected()) throw new IllegalArgumentException("topological order requires a directed graph");
        int[] indegree = graph.inDegrees();
        int[] originalIndegree = indegree.clone();
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        for (int vertex = 0; vertex < graph.vertexCount(); vertex++)
            if (indegree[vertex] == 0) queue.add(vertex);

        List<Integer> order = new ArrayList<>(graph.vertexCount());
        while (!queue.isEmpty()) {
            int vertex = queue.remove();
            order.add(vertex);
            for (Edge edge : graph.outgoing(vertex)) {
                metrics.edgeScans++;
                if (--indegree[edge.to()] == 0) queue.add(edge.to());
            }
        }
        return new TopologicalResult(order, originalIndegree, order.size() != graph.vertexCount());
    }
}
