/*
 * Copyright (C) 2017 ss
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package ss.sonya.transport.search;

import java.util.LinkedList;
import java.util.List;
import ss.sonya.entity.Path;

/**
 * Abstract path graph.
 * @author ss
 */
public class Graph {
    /** Path in vertex order. */
    private final List<Path> paths;
    /** Edges count. */
    private int edges;
    /** Lists of adjacency. */
    private final List<Integer[]>[] adj;
    /**
     * Constructor.
     * @param sortedPaths sorted paths.
     */
    public Graph(final List<Path> sortedPaths) {
        adj = new List[sortedPaths.size()];
        paths = sortedPaths;
        edges = 0;
        for (int i = 0; i < adj.length; i++) {
            adj[i] = new LinkedList<>();
        }
    }
    /**
     * Add new edge.
     * @param v vertex 1 (path 1).
     * @param w vertex 2 (path 2).
     * @param vb vertex 1: path #1 bus stop.
     * @param wb vertex 2: path #2 bus stop.
     */
    public void addEdge(int v, int w, int vb, int wb) {
        if (!isEdgeExist(v, w, vb, wb)) {
            adj[v].add(new Integer[] {w, vb, wb});
            edges++;
        }
    }
    /**
     * Get graph vertices count.
     * @return - vertices count.
     */
    public int vertices() {
        return adj.length;
    }
    /**
     * Get graph edges count.
     * @return - edges count.
     */
    public int edges() {
        return edges;
    }
    /**
     * Get vertex adjacency.
     * @param v - vertex order number.
     * @return - adjacency.
     */
    public List<Integer[]> adj(int v) {
        return adj[v];
    }
    /**
     * Get index path in graph.
     * @param path - path.
     * @return - index in graph.
     */
    public int indexOfPath(final Path path) {
        return paths.indexOf(path);
    }
    /**
     * Get path by graph index.
     * @param idx - graph index.
     * @return - path.
     */
    public Path getPath(final int idx) {
        return paths.get(idx);
    }
    /**
     * Find numbers of self loops.
     * @return - numbers of self loops.
     */
    public int numbersOfSelfLoops() {
        int count = 0;
        for (int v = 0; v < vertices(); v++) {
            for (Integer[] w : adj(v)) {
                if (v == w[0]) {
                    count++;
                }
            }
        }
        return count;
    }
    /**
     * Find vertex degree.
     * @param v - vertex order number.
     * @return - vertex degree.
     */
    public int vertexDegree(int v) {
        return adj[v].size();
    }
    /**
     * Find graph average degree.
     * @return graph average degree.
     */
    public int avgDegree() {
        return vertices() == 0 ? 0 : (2 * edges() / vertices());
    }
    /**
     * Find max graph degree.
     * @return - max degree.
     */
    public int maxDegree() {
        int max = 0;
        for (int v = 0; v < vertices(); v++) {
            if (vertexDegree(v) > max) {
                max = vertexDegree(v);
            }
        }
        return max;
    }
    /**
     * Print vertex.
     * @param v - vertex.
     * @return - string.
     */
    public String printVertex(int v) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\n*************** ").append(v).append(" **************\n");
        for (Integer[] w : adj(v)) {
            sb.append(String.format("%-6s", "[" + w[0] + "]:"));
            sb.append(String.format("%-6s", " [" + w[1] + "]"));
            sb.append(String.format("%-6s", " [" + w[2] + "]")).append("\n");
        }
        sb.append("edges: ").append(adj(v).size());
        sb.append("\n\n");
        return sb.toString();
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Path graph [ vertices=");
        sb.append(adj.length).append(", edges=").append(edges);
        sb.append(", density=").append(Math.log(edges) / Math.log(adj.length));
        sb.append(", self loops=").append(numbersOfSelfLoops());
        sb.append(", graph average degree=").append(avgDegree());
        sb.append(", graph degree=").append(2 * edges());
        sb.append(", vertex max degree=").append(maxDegree());
        sb.append(" ]");
        return sb.toString();
    }
// ================================ PRIVATE ===================================
    /**
     * Check if edge exist.
     * @param v - vertex.
     * @param w - other vertex.
     * @param vb - vertex bus stop.
     * @param wb - other vertex bus stop.
     * @return - true if exist.
     */
    private boolean isEdgeExist(int v, int w, int vb, int wb) {
        for (Integer[] e : adj(v)) {
            if (e[0] == w && e[1] == vb && e[2] == wb) {
                return true;
            }
        }
        return false;
    }
}
