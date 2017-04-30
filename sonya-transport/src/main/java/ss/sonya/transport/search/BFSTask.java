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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import org.apache.log4j.Logger;
import ss.sonya.entity.BusStop;
import ss.sonya.transport.search.vo.Decision;

/**
 * BFS task.
 * @author ss
 */
public class BFSTask implements Callable<List<Decision>> {
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(BFSTask.class);
    /** Start vertices criteria. */
    private final List<Integer> startCriteria;
    /** End vertices and it bus stops. */
    private final Map<Integer, Set<BusStop>> endVertices;
    /** Start vertices and it bus stops. */
    private final Map<Integer, Set<BusStop>> startVertices;
    /** Graph. */
    private final Graph graph;
    /** Search limit depth (max transfers). */
    private final int limitDepth;
    /**
     * Constructor.
     * @param pStartCriteria start vertices criteria.
     * @param pEndVertices end bus stops.
     * @param pStartVertices start bus stops.
     * @param pGraph graph.
     * @param pLimitDepth search limit depth.
     */
    public BFSTask(final List<Integer> pStartCriteria,
            final Map<Integer, Set<BusStop>> pEndVertices,
            final Map<Integer, Set<BusStop>> pStartVertices, final Graph pGraph,
            final int pLimitDepth) {
        startCriteria = pStartCriteria;
        endVertices = pEndVertices;
        startVertices = pStartVertices;
        graph = pGraph;
        limitDepth = pLimitDepth;
    }
    @Override
    public List<Decision> call() throws Exception {
        List<Decision> all = new ArrayList<>();
        for (Integer sV : startCriteria) {
            all.addAll(bfs(sV));
        }
        return all;
    }
    /**
     * BFS implementation.
     * @param sV start vertex.
     * @return list decisions.
     * @throws Exception method error.
     */
    private List<Decision> bfs(final Integer sV) throws Exception {
        Set<Integer> endCriteria = endVertices.keySet();
        List<Decision> result = new ArrayList<>();
        int[][] edgesTo = new int[limitDepth][graph.vertices()];
        boolean[] marked = new boolean[graph.vertices()];
        // end vertices marked as visited already
        endCriteria.forEach(v -> {
            marked[v] = true;
        });
        Queue<Integer> queue = new LinkedList<>();
        queue.add(sV);
        int depth = 0;
        int levelCount = 0;
        int w;
        while (!queue.isEmpty()) {
            if (levelCount == 0) {
                depth++;
                levelCount = queue.size();
                if (LOG.isTraceEnabled()) {
                    LOG.trace("#-bfs-# depth = " + depth
                            + " level count = " + levelCount);
                }
            }
            int v = queue.poll();
            levelCount--;
            for (Integer[] adj : graph.adj(v)) {
                w = adj[Graph.IDX_W];
                edgesTo[depth - 1][w] = v;
                if (endCriteria.contains(w)) {
                    // bingo! found potencial decision
                    // create way
                    int d = depth;
                    int[] way = new int[depth + 1];
                    for (int x = w; x != sV; x = edgesTo[--d][x]) {
                        way[d] = x;
                    }
                    way[0] = sV;
                    if (way.length > 0) {
                        for (BusStop startBs : startVertices.get(sV)) {
                            // TODO check way
                            for (BusStop endBs : endVertices.get(w)) {
                                result.add(new Decision(startBs, endBs, way));
                            }
                        }
                    }
                }
                // exclude duplicates from next level.
                if (!queue.contains(w) && !marked[w]) {
                    queue.add(w);
                }
            }
            marked[v] = true;
            // search while limit depth not reached
            if (depth == limitDepth) {
                break;
            }
        }
//        Map<String, Decision> set = new HashMap<>();
//        for (Decision d : result) {
//            set.put(d.toString(), d);
//        }
//        LOG.info("total [" + result.size() + "], rest [" + set.size()
//                + "], visits [" + visits + "]");
        return result;
    }
}
