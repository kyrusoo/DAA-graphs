package org.example.graph;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class DisjointSetUnionTest {
    @Test
    void randomOperationsMatchSimpleLabelReference() {
        int n = 100;
        int[] label = new int[n];
        for (int i = 0; i < n; i++) label[i] = i;
        DisjointSetUnion dsu = new DisjointSetUnion(n);
        Random random = new Random(42);
        int successful = 0;

        for (int operation = 0; operation < 2_000; operation++) {
            int a = random.nextInt(n), b = random.nextInt(n);
            boolean expected = label[a] != label[b];
            assertEquals(expected, dsu.union(a, b));
            if (expected) {
                int old = label[b], replacement = label[a];
                for (int i = 0; i < n; i++) if (label[i] == old) label[i] = replacement;
                successful++;
            }
            assertEquals(n - successful, dsu.components());
        }
        for (int i = 0; i < n; i++) for (int j = 0; j < n; j++)
            assertEquals(label[i] == label[j], dsu.find(i) == dsu.find(j));
        assertEquals(dsu.successfulUnions(), successful);
        assertEquals(dsu.findCalls(), 2L * 2_000 + 2L * n * n);
    }
}
