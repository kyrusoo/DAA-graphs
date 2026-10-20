package org.example.graph;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class SccAndTopologicalTest {
    @Test
    void kosarajuMatchesMutualReachabilityOnRandomGraphs() {
        Random random = new Random(19);
        for (int trial = 0; trial < 50; trial++) {
            int n = 1 + random.nextInt(30);
            Graph graph = new Graph(n, true);
            for (int e = 0; e < 3 * n; e++) graph.addEdge(random.nextInt(n), random.nextInt(n), 1);
            SccResult result = Kosaraju.find(graph, new Metrics());
            int[] component = new int[n];
            for (int id = 0; id < result.components().size(); id++)
                for (int vertex : result.components().get(id)) component[vertex] = id;
            boolean[][] reachable = new boolean[n][n];
            for (int start = 0; start < n; start++) {
                ArrayDeque<Integer> queue = new ArrayDeque<>();
                reachable[start][start] = true;
                queue.add(start);
                while (!queue.isEmpty()) for (Edge edge : graph.outgoing(queue.remove()))
                    if (!reachable[start][edge.to()]) {
                        reachable[start][edge.to()] = true;
                        queue.add(edge.to());
                    }
            }
            for (int a = 0; a < n; a++) for (int b = 0; b < n; b++)
                assertEquals(reachable[a][b] && reachable[b][a], component[a] == component[b]);
        }
    }

    @Test
    void kahnReturnsValidOrderAndDetectsCycles() throws Exception {
        Graph dag = new Graph(5, true);
        dag.addEdge(0, 2, 1);
        dag.addEdge(1, 2, 1);
        dag.addEdge(2, 3, 1);
        dag.addEdge(2, 4, 1);
        Metrics dagMetrics = new Metrics();
        TopologicalResult result = TopologicalSort.kahn(dag, dagMetrics);
        assertFalse(result.hasCycle());
        assertValidOrder(dag, result.order());
        assertEquals(dag.edges().size(), dagMetrics.edgeScans);

        Graph cyclic = Graph.read(Path.of("data/cyclic_directed.txt"));
        Metrics metrics = new Metrics();
        assertTrue(TopologicalSort.kahn(cyclic, metrics).hasCycle());
        assertTrue(metrics.edgeScans <= cyclic.edges().size());
    }

    @Test
    void iterativeKosarajuIsSafeOnOneHundredThousandVertexPath() {
        Graph path = new Graph(100_000, true);
        for (int v = 0; v + 1 < path.vertexCount(); v++) path.addEdge(v, v + 1, 1);
        Metrics metrics = new Metrics();
        SccResult result = assertDoesNotThrow(() -> Kosaraju.find(path, metrics));
        assertEquals(100_000, result.components().size());
        assertEquals(2L * path.edges().size(), metrics.edgeScans);
        assertTrue(metrics.maxDepth > 1);
    }

    private static void assertValidOrder(Graph graph, List<Integer> order) {
        int[] position = new int[graph.vertexCount()];
        for (int i = 0; i < order.size(); i++) position[order.get(i)] = i;
        for (Edge edge : graph.edges()) assertTrue(position[edge.from()] < position[edge.to()]);
    }
}
