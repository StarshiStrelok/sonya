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
import ss.sonya.entity.Path;
import ss.sonya.transport.constants.TransportConst;
import ss.sonya.transport.search.vo.Decision;
import ss.sonya.transport.search.vo.OptimalPath;

/**
 * BFS task.
 * @author ss
 */
public class BFSTask implements Callable<List<OptimalPath>> {
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
        limitDepth = pLimitDepth + 1;
    }
    @Override
    public List<OptimalPath> call() throws Exception {
//        long start = System.currentTimeMillis();
        List<Decision> all = new ArrayList<>();
        for (Integer sV : startCriteria) {
            all.addAll(bfs(sV));
        }
//        LOG.info("#-bfs-# bfs graph search time ["
//                + (System.currentTimeMillis() - start) + "]");
//        start = System.currentTimeMillis();
        List<OptimalPath> list = transformDecisions(all);
//        LOG.info("#-bfs-# bfs transform decisions time ["
//                + (System.currentTimeMillis() - start) + "]");
        return list;
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
        int vertices = graph.vertices();
        List<Integer>[][] edgesTo = new List[limitDepth][vertices];
        for (int m = 0; m < limitDepth; m++) {
            for (int n = 0; n < vertices; n++) {
                edgesTo[m][n] = new ArrayList<>();
            }
        }
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
                // search while limit depth not reached
                if (depth == limitDepth) {
                    break;
                }
            }
            int v = queue.poll();
            levelCount--;
            for (Integer[] adj : graph.adj(v)) {
                w = adj[Graph.IDX_W];
                edgesTo[depth - 1][w].add(v);
                if (endCriteria.contains(w)) {
                    // bingo! found potencial decision
                    // create way
                    List<Integer[]> ways = new ArrayList<>();
                    Integer[] sWay = new Integer[depth + 1];
                    sWay[sWay.length - 1] = w;
                    sWay[sWay.length - 2] = v;
                    if (v != sV) {
                        // if decisions exist, restrict all by length, exclude
                        // decisions which contains metro vertices
                        Integer restriction = result.isEmpty()
                                ? null : result.get(0).getWay().length;
                        int metroVCount = 0;
                        if (restriction != null) {
                            metroVCount += graph
                                    .metroVertices().contains(v) ? 1 : 0;
                            metroVCount += graph
                                    .metroVertices().contains(w) ? 1 : 0;
                        }
                        restoreDecisionLevel(
                                v, edgesTo, sWay, ways, sV, depth - 2,
                                restriction, metroVCount);
                    } else {
                        ways.add(sWay);
                    }

//                    List<Integer[]> ways = new ArrayList<>();
//                    if (v != sV) {
//                        // if decisions exist, restrict all by length, exclude
//                        // decisions which contains metro vertices
//                        Integer restriction = result.isEmpty()
//                                ? null : result.get(0).getWay().length;
//                        ways.addAll(restoreDecisionWays(v, w, edgesTo, sV, depth - 1, restriction));
//                    } else {
//                        Integer[] sWay = new Integer[depth + 1];
//                        sWay[sWay.length - 1] = w;
//                        sWay[sWay.length - 2] = v;
//                        ways.add(sWay);
//                    }
                    
                    for (Integer[] way : ways) {
                        for (BusStop startBs : startVertices.get(sV)) {
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
        }
//        Map<String, Decision> set = new HashMap<>();
//        for (Decision d : result) {
//            set.put(d.toString(), d);
//        }
//        LOG.info("total [" + result.size() + "], rest [" + set.size() + "]");
        return result;
    }
    /**
     * Restore one level of graph decision.
     * Recursive! Slowly.
     * @param v current vertex.
     * @param edgesTo edges array.
     * @param path current path.
     * @param result result array.
     * @param sV start vertex.
     * @param depth current depth (level).
     * @param restriction depth restriction for
     *                    ordinary (without metro) decisions.
     * @param metroVCount metro vertex count in decision.
     */
    private void restoreDecisionLevel(final int v,
            final List<Integer>[][] edgesTo, final Integer[] path,
            final List<Integer[]> result, final int sV, final int depth,
            final Integer restriction, final int metroVCount) {
        for (Integer w : edgesTo[depth][v]) {
            int newMetroVCount = 0;
            if (restriction != null) {
                newMetroVCount = metroVCount
                        + (graph.metroVertices().contains(w) ? 1 : 0);
                if (path.length >= restriction) {
                    if (path.length - newMetroVCount > restriction) {
                        continue;
                    }
                }
            }
            if (sV == w) {
                path[0] = w;
                result.add(path);
            } else {
                Integer[] copyPath = new Integer[path.length];
                System.arraycopy(path, 0, copyPath, 0, path.length);
                copyPath[depth] = w;
                restoreDecisionLevel(w, edgesTo, copyPath, result, sV,
                        depth - 1, restriction, newMetroVCount);
            }
        }
    }
    private List<Integer[]> restoreDecisionWays(final int v, final int eV,
            final List<Integer>[][] edgesTo, final int sV, final int depth,
            final Integer restriction) {
        List<Integer[]> ways = new ArrayList<>();
        Integer[] sWay = new Integer[2];
        sWay[1] = eV;
        sWay[0] = v;
        ways.add(sWay);
        int curV;
        int curDepth = depth;
        List<Integer> edges;
        List<Integer[]> parentWays = new ArrayList<>();
        Set<Integer> metroVertices = graph.metroVertices();
        Queue<Integer> queue = new LinkedList<>();
        queue.add(v);
        while (curDepth > 0) {
            curV = queue.poll();
            edges = edgesTo[curDepth - 1][curV];
            if (queue.isEmpty()) {
                curDepth--;
            }
            parentWays.clear();
            for (Integer[] subWay : ways) {
                if (subWay[0] == curV) {
                    parentWays.add(subWay);
                }
            }
            for (Integer edge : edges) {
                if (sV != edge) {
                    queue.add(edge);
                }
                for (Integer[] parentWay : parentWays) {
                    Integer[] copyWay = new Integer[parentWay.length + 1];
                    for (int k = 1; k < copyWay.length; k++) {
                        copyWay[k] = parentWay[k - 1];
                    }
                    copyWay[0] = edge;
                    if (restriction != null && copyWay.length > restriction) {
                        int metroCount = 0;
                        for (Integer i : copyWay) {
                            if (metroVertices.contains(i)) {
                                metroCount++;
                            }
                        }
                        if ((copyWay.length + 1) - metroCount <= restriction) {
                            ways.add(copyWay);
                        }
                    } else {
                        ways.add(copyWay);
                    }
                }
            }
            ways.removeAll(parentWays);
        }
        return ways;
    }
    /**
     * Transform found decisions to optimal paths.
     * @param list list decisions.
     * @return list optimal paths.
     */
    private List<OptimalPath> transformDecisions(final List<Decision> list) {
        List<OptimalPath> rest = new LinkedList<>();
        int idxS, idxT, idxE;
        Integer[] way;
        Queue<Integer> queue = new LinkedList<>();
        for (Decision decision : list) {
            queue.clear();
            way = decision.getWay();
            idxS = graph.getPath(way[0]).getBusstops().indexOf(decision.getS());
            idxE = graph.getPath(way[way.length - 1]).getBusstops()
                    .lastIndexOf(decision.getE());
            if (idxS == -1 || idxE == -1) {
                throw new RuntimeException("incorrect decision!");
            }
            idxT = idxS;
            boolean isRight = true;
            queue.add(idxS);
            for (int k = 0; k < way.length; k++) {
                int v = way[k];             // current vertex
                if (way.length - 1 == k) {  // end vertex
                    if (idxT != -1 && idxT >= idxE) {
                        idxT = -1;
                    } else {
                        queue.add(idxE);
                    }
                } else {                    // start and middle vertices
                    int w = way[k + 1];     // next vertex
                    Integer[] adjW = null;
                    for (Integer[] adj : graph.adj(v)) {
                        if (adj[Graph.IDX_W] == w) {
                            adjW = adj;
                            break;
                        }
                    }
                    if (idxT != -1) {
                        int v1 = -1;
                        int w1 = -1;
                        double checkTotal = Double.MAX_VALUE;
                        for (int j = 1; j < adjW.length; j += 2) {
                            int vt = adjW[j];
                            if (idxT < vt) {
                                int tV = vt + 1;
                                int tW = graph.getPath(w).getBusstops()
                                        .size() - (adjW[j + 1] + 1);
                                double newTotal = (tV / graph.getPath(v)
                                        .getRoute().getType().getAvgSpeed())
                                    + (tW / graph.getPath(w).getRoute()
                                            .getType().getAvgSpeed());
                                if (v1 == -1) {
                                    v1 = vt;
                                    w1 = adjW[j + 1];
                                    checkTotal = newTotal;
                                } else {
                                    // check total bus stop count for two paths
                                    if (checkTotal > newTotal) {
                                        v1 = vt;
                                        w1 = adjW[j + 1];
                                        checkTotal = newTotal;
                                    }
                                }
                            }
                        }
                        if (v1 != -1) {
                            queue.add(v1);
                            queue.add(w1);
                            idxT = w1;
                        } else {
                            idxT = -1;
                        }
                    }
                }
                if (idxT == -1) {
                    isRight = false;
                    break;
                }
            }
            if (isRight) {
                OptimalPath op = new OptimalPath();
                List<Path> paths = new ArrayList<>();
                List<List<BusStop>> pathsWay = new ArrayList<>();
                int transfers = 0;
                for (int i = 0; i < way.length; i++) {
                    int v = way[i];
                    int s = queue.poll();
                    int e = queue.poll();
                    Path p = graph.getPath(v);
                    if (!TransportConst.METRO.equals(
                            p.getRoute().getType().getName())) {
                        transfers++;
                    }
                    paths.add(p);
                    pathsWay.add(p.getBusstops().subList(s, e + 1));
                }
                op.setTransfers(transfers);
                op.setPath(paths);
                op.setWay(pathsWay);
                op.setDecision(decision);
                op.setWay(pathsWay);
                rest.add(op);
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.trace("#-bfs-# remove [" + (list.size() - rest.size())
                    + "] unreal decisions from [" + list.size() + "]");
        }
        return rest;
    }
}
