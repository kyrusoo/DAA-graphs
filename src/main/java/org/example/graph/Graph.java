package org.example.graph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class Graph {
    private final int vertices;
    private final boolean directed;
    private final List<List<Edge>> adjacency;
    private final List<Edge> edges = new ArrayList<>();

    public Graph(int vertices, boolean directed) {
        if (vertices < 0) throw new IllegalArgumentException("negative vertex count");
        this.vertices = vertices;
        this.directed = directed;
        adjacency = new ArrayList<>(vertices);
        for (int i = 0; i < vertices; i++) adjacency.add(new ArrayList<>());
    }

    public void addEdge(int from, int to, int weight) {
        checkVertex(from);
        checkVertex(to);
        Edge edge = new Edge(from, to, weight);
        edges.add(edge);
        adjacency.get(from).add(edge);
        if (!directed) adjacency.get(to).add(new Edge(to, from, weight));
    }

    public int vertexCount() { return vertices; }
    public boolean isDirected() { return directed; }
    public List<Edge> edges() { return edges; }
    public List<Edge> outgoing(int vertex) { checkVertex(vertex); return adjacency.get(vertex); }

    public Graph reversed() {
        Graph reverse = new Graph(vertices, true);
        for (Edge edge : edges) reverse.addEdge(edge.to(), edge.from(), edge.weight());
        return reverse;
    }

    public int[] inDegrees() {
        int[] result = new int[vertices];
        for (Edge edge : edges) result[edge.to()]++;
        return result;
    }

    public static Graph read(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path);
        int firstLine = 0;
        while (firstLine < lines.size() && lines.get(firstLine).isBlank()) firstLine++;
        if (firstLine == lines.size()) throw new IllegalArgumentException("empty graph file");
        String[] header = lines.get(firstLine).trim().split("\\s+");
        if (header.length != 3) throw new IllegalArgumentException("expected: n m directed|undirected");
        int n = Integer.parseInt(header[0]);
        int m = Integer.parseInt(header[1]);
        boolean directed = switch (header[2].toLowerCase()) {
            case "directed" -> true;
            case "undirected" -> false;
            default -> throw new IllegalArgumentException("graph type must be directed or undirected");
        };
        Graph graph = new Graph(n, directed);
        int read = 0;
        for (int i = firstLine + 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) continue;
            String[] values = line.trim().split("\\s+");
            if (values.length != 3) throw new IllegalArgumentException("expected edge: u v w");
            graph.addEdge(Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]));
            read++;
        }
        if (read != m) throw new IllegalArgumentException("expected " + m + " edges, found " + read);
        return graph;
    }

    private void checkVertex(int vertex) {
        if (vertex < 0 || vertex >= vertices) throw new IndexOutOfBoundsException("vertex " + vertex);
    }
}
