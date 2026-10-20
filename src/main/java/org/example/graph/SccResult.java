package org.example.graph;

import java.util.List;

public record SccResult(List<List<Integer>> components) {
    public SccResult {
        components = components.stream().map(List::copyOf).toList();
    }
}
