package org.example.graph;

public final class DisjointSetUnion {
    private final int[] parent;
    private final int[] rank;
    private final Metrics metrics;
    private int components;
    private long findCalls;
    private long successfulUnions;

    public static DisjointSetUnion makeSet(int size) { return new DisjointSetUnion(size); }

    public DisjointSetUnion(int size) { this(size, new Metrics()); }

    public DisjointSetUnion(int size, Metrics metrics) {
        if (size < 0) throw new IllegalArgumentException("negative set count");
        this.metrics = metrics;
        parent = new int[size];
        rank = new int[size];
        components = size;
        for (int i = 0; i < size; i++) parent[i] = i;
    }

    public int find(int item) {
        checkItem(item);
        findCalls++;
        metrics.dsuOps++;
        int root = item;
        while (root != parent[root]) root = parent[root];
        while (item != root) {
            int next = parent[item];
            parent[item] = root;
            item = next;
        }
        return root;
    }

    public boolean union(int first, int second) {
        int root1 = find(first);
        int root2 = find(second);
        if (root1 == root2) return false;
        if (rank[root1] < rank[root2]) {
            parent[root1] = root2;
        } else if (rank[root1] > rank[root2]) {
            parent[root2] = root1;
        } else {
            parent[root2] = root1;
            rank[root1]++;
        }
        components--;
        successfulUnions++;
        metrics.dsuOps++;
        return true;
    }

    public int components() { return components; }
    public long findCalls() { return findCalls; }
    public long successfulUnions() { return successfulUnions; }

    private void checkItem(int item) {
        if (item < 0 || item >= parent.length) throw new IndexOutOfBoundsException("item " + item);
    }
}
