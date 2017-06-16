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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import ss.sonya.entity.Path;

/**
 * Transport path graph.
 *
 * Graph structure.
 * One graph vertex - one path, all metro paths wrap to 0 vertex, because
 * it reduce size of the main graph, and metro regarded as independent
 * underground transport type.
 * Graph edge is 5-elements array: first element - vertex number of transfer
 * path, second element - end bus stop order of vertex way (or ID if vertex path
 * is metro), third element - transfer path start bus stop order (or bus stop
 * ID if vertex path is metro).
 *
 *  V - current vertex
 *  E[] - array transfer vertices
 *
 *  V   E   E   E
 * [0] [1] [4] [4]  Vertex number of transfer path
 *     [5] [2] [3]  Bus stop order in way for V-path (first transfer).
 *     [8] [5] [1]  Bus stop order in way for E-path (first transfer).
 *     [1] [3] [6]  Bus stop order in way for V-path (second transfer).
 *     [1] [3] [6]  Bus stop order in way for E-path (second transfer).
 *
 * Path (exclude metro) can has no more then 2 transfers for
 * same transfer path: first and last crossing, middle crossing missing.
 *
 * @author ss
 */
public class Graph {
    /** Adjacency array empty value. */
    public static final int IDX_NULL = -1;
    /** Adjacency vertex index. Everywhere is denoted as 'w'. */
    public static final int IDX_W = 0;
    /** Vertex transfer bus stop order (index) in way. */
    public static final int IDX_V_TRANSFER_1 = 1;
    /** Adjacency vertex transfer bus stop order (index) in way. */
    public static final int IDX_W_TRANSFER_1 = 2;
    /** Vertex bus stop order (index) in way for second transfer. */
    public static final int IDX_V_TRANSFER_2 = 3;
    /** Adjacency vertex bus stop order (index) in way for second transfer. */
    public static final int IDX_W_TRANSFER_2 = 4;
    /** Path in vertex order. */
    private final List<Path> paths;
    /** Path schedule cache. Path/Days/Bus stop times/separate time. */
    private final Map<Path, Map<String, List<List<String>>>> scheduleMap =
            new HashMap<>();
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
     * @param tInfo  transfer information.
     *        tInfo[0] vertex 1: path #1 bus stop (first transfer).
     *        tInfo[1] vertex 2: path #2 bus stop (first transfer).
     *        tInfo[2] vertex 1: path #1 bus stop (second transfer).
     *        tInfo[3] vertex 2: path #2 bus stop (second transfer)...
     */
    public void addEdge(int v, int w, int[] tInfo) {
        Integer[] e = new Integer[tInfo.length + 1];
        e[IDX_W] = w;
        for (int i = 0; i < tInfo.length; i++) {
            e[i + 1] = tInfo[i];
        }
        adj[v].add(e);
        edges++;
    }
    /**
     * Check if way in graph exist.
     * @param way graph way.
     * @return true if exist.
     */
    public boolean isWayExist(final int[] way) {
        for (int i = 0; i < way.length - 1; i++) {
            int v = way[i];
            int w = way[i + 1];
            boolean isExist = false;
            for (Integer[] wt : adj(v)) {
                if (wt[IDX_W] == w) {
                    isExist = true;
                    break;
                }
            }
            if (!isExist) {
                return false;
            }
        }
        return true;
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
     * Get graph paths.
     * @return all paths.
     */
    public List<Path> getPaths() {
        return paths;
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
     * Put schedule to graph.
     * @param p path.
     * @param sch schedule.
     */
    public void putSchedule(final Path p,
            final Map<String, List<List<String>>> sch) {
        scheduleMap.put(p, sch);
    }
    /**
     * Get path schedule.
     * @param p path.
     * @return schedule, broken by days.
     */
    public Map<String, List<List<String>>> getSchedule(final Path p) {
        return scheduleMap.get(p);
    }
    /**
     * Get all graph paths.
     * @return all graph paths.
     */
    public List<Path> getAllPaths() {
        return paths;
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
            for (int k = 0; k < w.length; k++) {
                if (k == 0) {
                    sb.append(String.format("%-6s", "[" + w[k] + "]:"));
                } else {
                    sb.append(String.format("%-6s", " [" + w[k] + "]"));
                }
            }
            sb.append("\n");
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
}
