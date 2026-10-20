package org.example.graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public final class Kosaraju {
    private Kosaraju() { }

    public static SccResult find(Graph graph, Metrics metrics) {
        if (!graph.isDirected()) throw new IllegalArgumentException("SCC requires a directed graph");
        int n = graph.vertexCount();
        boolean[] visited = new boolean[n];
        List<Integer> finishOrder = new ArrayList<>(n);

        for (int start = 0; start < n; start++) {
            if (visited[start]) continue;
            ArrayDeque<Frame> stack = new ArrayDeque<>();
            visited[start] = true;
            stack.push(new Frame(start));
            metrics.maxDepth = Math.max(metrics.maxDepth, stack.size());
            while (!stack.isEmpty()) {
                Frame frame = stack.peek();
                List<Edge> outgoing = graph.outgoing(frame.vertex);
                if (frame.nextEdge < outgoing.size()) {
                    Edge edge = outgoing.get(frame.nextEdge++);
                    metrics.edgeScans++;
                    if (!visited[edge.to()]) {
                        visited[edge.to()] = true;
                        stack.push(new Frame(edge.to()));
                        metrics.maxDepth = Math.max(metrics.maxDepth, stack.size());
                    }
                } else {
                    finishOrder.add(frame.vertex);
                    stack.pop();
                }
            }
        }

        Graph reverse = graph.reversed();
        java.util.Arrays.fill(visited, false);
        List<List<Integer>> components = new ArrayList<>();
        for (int i = finishOrder.size() - 1; i >= 0; i--) {
            int start = finishOrder.get(i);
            if (visited[start]) continue;
            List<Integer> component = new ArrayList<>();
            ArrayDeque<Integer> stack = new ArrayDeque<>();
            stack.push(start);
            visited[start] = true;
            metrics.maxDepth = Math.max(metrics.maxDepth, stack.size());
            while (!stack.isEmpty()) {
                int vertex = stack.pop();
                component.add(vertex);
                for (Edge edge : reverse.outgoing(vertex)) {
                    metrics.edgeScans++;
                    if (!visited[edge.to()]) {
                        visited[edge.to()] = true;
                        stack.push(edge.to());
                        metrics.maxDepth = Math.max(metrics.maxDepth, stack.size());
                    }
                }
            }
            components.add(component);
        }
        return new SccResult(components);
    }

    private static final class Frame {
        private final int vertex;
        private int nextEdge;
        private Frame(int vertex) { this.vertex = vertex; }
    }
}
