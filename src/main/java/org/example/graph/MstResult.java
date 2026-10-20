package org.example.graph;

import java.util.List;

public record MstResult(long totalWeight, int components, List<Edge> edges) {
    public MstResult {
        edges = List.copyOf(edges);
    }
}
