package org.example.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public final class Plotter {
    private static final Color[] COLORS = {
            new Color(35, 110, 190), new Color(230, 120, 35), new Color(45, 155, 95),
            new Color(190, 65, 65), new Color(130, 85, 180), new Color(0, 145, 155),
            new Color(210, 170, 35), new Color(90, 90, 90), new Color(215, 90, 160),
            new Color(110, 145, 50)
    };

    private Plotter() { }

    public static void write(List<CsvRow> rows, Path directory) throws IOException {
        List<Series> operations = new ArrayList<>();
        List<Series> scansPerEdge = new ArrayList<>();
        for (String algorithm : List.of("Kruskal", "Prim", "Kosaraju", "Kahn", "DagShortestPath")) {
            for (String density : List.of("sparse", "dense")) {
                List<CsvRow> selected = rows.stream().filter(r -> r.algorithm().equals(algorithm) &&
                        r.graph().endsWith(density)).toList();
                String name = algorithm + " " + density;
                operations.add(series(name, selected, r -> r.dsuOps() + r.edgeScans() + r.comparisons()));
                scansPerEdge.add(series(name, selected, r -> (double) r.edgeScans() / r.m()));
            }
        }
        List<Series> kruskalShare = List.of(
                series("sparse", rows.stream().filter(r -> r.algorithm().equals("Kruskal") &&
                        r.graph().endsWith("sparse")).toList(), r -> (double) r.edgeScans() / r.m()),
                series("dense", rows.stream().filter(r -> r.algorithm().equals("Kruskal") &&
                        r.graph().endsWith("dense")).toList(), r -> (double) r.edgeScans() / r.m()));

        draw(directory.resolve("operations_vs_n.png"), "Counted work vs vertices", "Vertices (n)",
                "DSU ops + edge scans + comparisons", operations, true, true);
        draw(directory.resolve("scans_per_edge.png"), "Edge scans per input edge", "Vertices (n)",
                "Edge scans / m", scansPerEdge, true, false);
        draw(directory.resolve("kruskal_share.png"), "Share of edges examined by Kruskal", "Vertices (n)",
                "Kruskal edge scans / m", kruskalShare, true, false);
    }

    private static Series series(String name, List<CsvRow> rows, Value value) {
        List<Point> points = rows.stream().map(row -> new Point(row.n(), value.get(row))).toList();
        return new Series(name, points);
    }

    private static void draw(Path path, String title, String xLabel, String yLabel,
                             List<Series> series, boolean logX, boolean logY) throws IOException {
        int width = 1_400, height = 820, left = 100, top = 80, right = 350, bottom = 110;
        int plotWidth = width - left - right, plotHeight = height - top - bottom;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE); g.fillRect(0, 0, width, height);
        g.setFont(new Font("SansSerif", Font.BOLD, 24));
        g.setColor(new Color(35, 40, 48)); g.drawString(title, left, 40);

        List<Point> all = series.stream().flatMap(s -> s.points.stream()).toList();
        double minX = all.stream().mapToDouble(p -> tx(p.x, logX)).min().orElse(0);
        double maxX = all.stream().mapToDouble(p -> tx(p.x, logX)).max().orElse(1);
        double minY = all.stream().mapToDouble(p -> ty(p.y, logY)).min().orElse(0);
        double maxY = all.stream().mapToDouble(p -> ty(p.y, logY)).max().orElse(1);
        double yPad = (maxY - minY) * 0.08;
        if (yPad == 0) yPad = 0.5;
        minY -= yPad; maxY += yPad;

        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        for (int tick = 0; tick <= 5; tick++) {
            int y = top + plotHeight - tick * plotHeight / 5;
            double transformed = minY + tick * (maxY - minY) / 5;
            double label = logY ? Math.pow(10, transformed) : transformed;
            g.setColor(new Color(225, 229, 234)); g.drawLine(left, y, left + plotWidth, y);
            g.setColor(new Color(70, 75, 82));
            g.drawString(String.format(java.util.Locale.ROOT, "%.3g", label), 20, y + 5);
        }
        for (int tick = 0; tick < 5; tick++) {
            int x = left + tick * plotWidth / 4;
            double transformed = minX + tick * (maxX - minX) / 4;
            double label = logX ? Math.pow(10, transformed) : transformed;
            g.setColor(new Color(225, 229, 234)); g.drawLine(x, top, x, top + plotHeight);
            g.setColor(new Color(70, 75, 82));
            g.drawString(formatNumber(label), x - 22, top + plotHeight + 24);
        }
        g.setColor(new Color(45, 50, 56));
        g.setStroke(new BasicStroke(1.6f));
        g.drawLine(left, top, left, top + plotHeight);
        g.drawLine(left, top + plotHeight, left + plotWidth, top + plotHeight);
        g.setFont(new Font("SansSerif", Font.PLAIN, 17));
        g.drawString(xLabel, left + plotWidth / 2 - 35, height - 25);
        g.drawString(yLabel, left + 10, top - 18);

        for (int i = 0; i < series.size(); i++) {
            Series line = series.get(i);
            Color color = COLORS[i % COLORS.length];
            g.setColor(color); g.setStroke(new BasicStroke(2.4f));
            for (int j = 1; j < line.points.size(); j++) {
                Point a = line.points.get(j - 1), b = line.points.get(j);
                g.drawLine(px(a.x, minX, maxX, logX, left, plotWidth),
                        py(a.y, minY, maxY, logY, top, plotHeight),
                        px(b.x, minX, maxX, logX, left, plotWidth),
                        py(b.y, minY, maxY, logY, top, plotHeight));
            }
            for (Point point : line.points) {
                int x = px(point.x, minX, maxX, logX, left, plotWidth);
                int y = py(point.y, minY, maxY, logY, top, plotHeight);
                g.fillOval(x - 4, y - 4, 8, 8);
            }
            int legendY = top + 24 + i * 25;
            g.setStroke(new BasicStroke(3)); g.drawLine(left + plotWidth + 30, legendY, left + plotWidth + 58, legendY);
            g.setColor(new Color(45, 50, 56)); g.drawString(line.name, left + plotWidth + 68, legendY + 5);
        }
        g.dispose();
        ImageIO.write(image, "png", path.toFile());
    }

    private static int px(double x, double min, double max, boolean log, int left, int width) {
        double value = tx(x, log);
        return left + (int) Math.round((value - min) / (max - min) * width);
    }

    private static int py(double y, double min, double max, boolean log, int top, int height) {
        double value = ty(y, log);
        return top + height - (int) Math.round((value - min) / (max - min) * height);
    }

    private static double tx(double value, boolean log) { return log ? Math.log10(value) : value; }
    private static double ty(double value, boolean log) { return log ? Math.log10(value) : value; }
    private static String formatNumber(double number) {
        if (number >= 1_000) return String.format(java.util.Locale.ROOT, "%.0fk", number / 1_000);
        return String.format(java.util.Locale.ROOT, "%.0f", number);
    }

    @FunctionalInterface private interface Value { double get(CsvRow row); }
    private record Point(double x, double y) { }
    private record Series(String name, List<Point> points) { }
}
