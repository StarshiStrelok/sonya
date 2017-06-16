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
import java.util.HashMap;
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
        limitDepth = pLimitDepth;
    }
    @Override
    public List<OptimalPath> call() throws Exception {
        List<Decision> all = new ArrayList<>();
        for (Integer sV : startCriteria) {
            all.addAll(bfs(sV));
        }
        return transformDecisions(all);
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
                    Integer[] stub = new Integer[depth + 1];
                    stub[stub.length - 1] = w;
                    restoreLevel(w, edgesTo, stub, ways, sV, depth - 1);
                    for (Integer[] way : ways) {
                        for (BusStop startBs : startVertices.get(sV)) {
                            for (BusStop endBs : endVertices.get(w)) {
                                Decision dec = new Decision(startBs, endBs, way);
                                if (dec.toString().contains("914")
                                        && dec.toString().contains("912")) {
                                    System.out.println(dec);
                                }
                                result.add(dec);
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
        Map<String, Decision> set = new HashMap<>();
        for (Decision d : result) {
            set.put(d.toString(), d);
        }
        LOG.info("total [" + result.size() + "], rest [" + set.size() + "]");
        return new ArrayList<>(set.values());
    }
    private void restoreLevel(final int v,
            final List<Integer>[][] edgesTo, final Integer[] path,
            final List<Integer[]> result, final int sV, final int depth) {
        List<Integer> tV = edgesTo[depth][v];
        for (Integer w : tV) {
            if (sV == w) {
                path[0] = w;
                result.add(path);
            } else {
                Integer[] copyPath = new Integer[path.length];
                System.arraycopy(path, 0, copyPath, 0, path.length);
                copyPath[depth] = w;
                restoreLevel(w, edgesTo, copyPath, result, sV, depth - 1);
            }
        }
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
                        for (int j = 1; j < adjW.length; j += 2) {
                            int vt = adjW[j];
                            if (idxT < vt) {
                                if (v1 == -1) {
                                    v1 = vt;
                                    w1 = adjW[j + 1];
                                } else if (v1 > vt) {
                                    v1 = vt;
                                    w1 = adjW[j + 1];
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
                    if (paths.size() > 0 && TransportConst.METRO.equals(
                            p.getRoute().getType().getName())
                            && paths.get(paths.size() - 1)
                                    .getRoute().getType().getName()
                                    .equals(TransportConst.METRO)) {
                        transfers--;
                    }
                    transfers++;
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
