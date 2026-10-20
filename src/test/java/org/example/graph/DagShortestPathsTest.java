package org.example.graph;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class DagShortestPathsTest {
    @Test
    void dagRelaxationMatchesBellmanFordWithNegativeWeights() {
        Random random = new Random(2026);
        for (int trial = 0; trial < 100; trial++) {
            int n = 1 + random.nextInt(30);
            int[] order = new int[n];
            for (int i = 0; i < n; i++) order[i] = i;
            for (int i = n - 1; i > 0; i--) {
                int j = random.nextInt(i + 1), temp = order[i];
                order[i] = order[j]; order[j] = temp;
            }
            Graph graph = new Graph(n, true);
            for (int i = 0; i < n; i++) for (int j = i + 1; j < n; j++)
                if (random.nextDouble() < 0.2) graph.addEdge(order[i], order[j], random.nextInt(41) - 20);

            TopologicalResult topo = TopologicalSort.kahn(graph, new Metrics());
            Metrics metrics = new Metrics();
            ShortestPathResult result = DagShortestPaths.solve(graph, topo, metrics);
            long[] expected = bellmanFordFromAllSources(graph);
            assertArrayEquals(expected, result.distances());
            assertEquals(graph.edges().size(), metrics.edgeScans);
            for (int target = 0; target < n; target++) {
                var path = result.pathTo(target);
                assertEquals(target, path.get(path.size() - 1));
                assertEquals(result.distances()[target], weightOfPath(graph, path));
            }
        }
    }

    private static long[] bellmanFordFromAllSources(Graph graph) {
        long inf = Long.MAX_VALUE / 4;
        long[] distance = new long[graph.vertexCount()];
        Arrays.fill(distance, inf);
        int[] indegree = graph.inDegrees();
        for (int v = 0; v < distance.length; v++) if (indegree[v] == 0) distance[v] = 0;
        for (int i = 1; i < distance.length; i++) {
            boolean changed = false;
            for (Edge edge : graph.edges()) if (distance[edge.from()] != inf &&
                    distance[edge.from()] + edge.weight() < distance[edge.to()]) {
                distance[edge.to()] = distance[edge.from()] + edge.weight();
                changed = true;
            }
            if (!changed) break;
        }
        return distance;
    }

    private static long weightOfPath(Graph graph, java.util.List<Integer> path) {
        long total = 0;
        for (int i = 1; i < path.size(); i++) {
            int from = path.get(i - 1), to = path.get(i);
            int best = Integer.MAX_VALUE;
            for (Edge edge : graph.outgoing(from)) if (edge.to() == to) best = Math.min(best, edge.weight());
            assertNotEquals(Integer.MAX_VALUE, best);
            total += best;
        }
        return total;
    }
}
