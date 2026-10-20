package org.example.graph;

import java.util.Random;

public final class GraphGenerator {
    private GraphGenerator() { }

    public static Graph undirected(int n, int m) {
        if (m < n - 1) throw new IllegalArgumentException("spanning tree requires at least n-1 edges");
        Random random = new Random(42);
        int[] permutation = shuffled(n, random);
        Graph graph = new Graph(n, false);
        for (int i = 1; i < n; i++)
            graph.addEdge(permutation[random.nextInt(i)], permutation[i], weight(random));
        for (int i = n - 1; i < m; i++) {
            int a = random.nextInt(n), b = random.nextInt(n);
            while (b == a) b = random.nextInt(n);
            graph.addEdge(a, b, weight(random));
        }
        return graph;
    }

    public static Graph directed(int n, int m) {
        Random random = new Random(42);
        Graph graph = new Graph(n, true);
        for (int i = 0; i < m; i++) {
            int a = random.nextInt(n), b = random.nextInt(n);
            while (b == a) b = random.nextInt(n);
            graph.addEdge(a, b, weight(random));
        }
        return graph;
    }

    public static Graph dag(int n, int m) {
        Random random = new Random(42);
        int[] rank = shuffled(n, random);
        Graph graph = new Graph(n, true);
        for (int i = 0; i < m; i++) {
            int a = random.nextInt(n), b = random.nextInt(n);
            while (b == a) b = random.nextInt(n);
            if (rank[a] > rank[b]) {
                int temp = a; a = b; b = temp;
            }
            graph.addEdge(a, b, weight(random));
        }
        return graph;
    }

    private static int[] shuffled(int n, Random random) {
        int[] values = new int[n];
        for (int i = 0; i < n; i++) values[i] = i;
        for (int i = n - 1; i > 0; i--) {
            int j = random.nextInt(i + 1), temp = values[i];
            values[i] = values[j]; values[j] = temp;
        }
        return values;
    }

    private static int weight(Random random) { return 1 + random.nextInt(1_000); }
}
