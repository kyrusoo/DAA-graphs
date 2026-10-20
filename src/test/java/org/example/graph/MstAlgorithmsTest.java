package org.example.graph;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class MstAlgorithmsTest {
    @Test
    void connectedExampleInputProducesExpectedTree() throws Exception {
        Graph graph = Graph.read(Path.of("data/connected_undirected.txt"));
        MstResult kruskal = MstAlgorithms.kruskal(graph, new Metrics());
        MstResult prim = MstAlgorithms.prim(graph, new Metrics());
        assertEquals(13, kruskal.totalWeight());
        assertEquals(kruskal.totalWeight(), prim.totalWeight());
        assertEquals(graph.vertexCount() - 1, prim.edges().size());
    }

    @Test
    void kruskalAndPrimAgreeOnOneHundredConnectedGraphs() {
        Random random = new Random(71);
        for (int trial = 0; trial < 100; trial++) {
            int n = 2 + random.nextInt(30);
            Graph graph = new Graph(n, false);
            for (int v = 1; v < n; v++) graph.addEdge(v, random.nextInt(v), random.nextInt(2_001) - 1_000);
            for (int e = 0; e < 2 * n; e++) {
                int a = random.nextInt(n), b = random.nextInt(n);
                graph.addEdge(a, b, random.nextInt(2_001) - 1_000);
            }

            MstResult kruskal = MstAlgorithms.kruskal(graph, new Metrics());
            MstResult prim = MstAlgorithms.prim(graph, new Metrics());
            assertEquals(kruskal.totalWeight(), prim.totalWeight());
            assertEquals(1, kruskal.components());
            assertEquals(n - 1, kruskal.edges().size());
            assertEquals(n - 1, prim.edges().size());
        }
    }

    @Test
    void disconnectedInputReturnsMinimumSpanningForest() throws Exception {
        Graph graph = Graph.read(Path.of("data/disconnected_undirected.txt"));
        MstResult kruskal = MstAlgorithms.kruskal(graph, new Metrics());
        MstResult prim = MstAlgorithms.prim(graph, new Metrics());
        assertEquals(15, kruskal.totalWeight());
        assertEquals(kruskal.totalWeight(), prim.totalWeight());
        assertEquals(3, kruskal.components());
        assertEquals(graph.vertexCount() - 3, prim.edges().size());
    }
}
