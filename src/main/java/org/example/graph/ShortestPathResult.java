package org.example.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record ShortestPathResult(long[] distances, int[] parents) {
    public ShortestPathResult {
        distances = distances.clone();
        parents = parents.clone();
    }

    @Override
    public long[] distances() { return distances.clone(); }
    @Override
    public int[] parents() { return parents.clone(); }

    public long sumDistances() {
        long total = 0;
        for (long distance : distances) total += distance;
        return total;
    }

    public List<Integer> pathTo(int target) {
        if (target < 0 || target >= parents.length) throw new IndexOutOfBoundsException("vertex " + target);
        List<Integer> path = new ArrayList<>();
        for (int vertex = target; vertex != -1; vertex = parents[vertex]) path.add(vertex);
        Collections.reverse(path);
        return List.copyOf(path);
    }
}
