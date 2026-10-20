package org.example.graph;

import java.util.List;

public record TopologicalResult(List<Integer> order, int[] initialIndegrees, boolean hasCycle) {
    public TopologicalResult {
        order = List.copyOf(order);
        initialIndegrees = initialIndegrees.clone();
    }

    @Override
    public int[] initialIndegrees() { return initialIndegrees.clone(); }
}
