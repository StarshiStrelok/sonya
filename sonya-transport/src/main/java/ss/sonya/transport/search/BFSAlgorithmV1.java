/*
 * Copyright (C) 2017 ss
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ss.sonya.transport.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import ss.sonya.constants.TransportConst;
import ss.sonya.entity.BusStop;
import ss.sonya.entity.Path;
import ss.sonya.entity.TransportProfile;
import ss.sonya.transport.search.vo.OptimalPath;
import ss.sonya.transport.search.vo.SearchSettings;

/**
 * Search engine, based on breadth-first search algorithm for graphs.
 * @author ss
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class BFSAlgorithmV1 extends BFS implements SearchEngine {
    /** Limit time multiplexer. max_time=min_time * multiplexer. */
    private static final double LIMIT_TIME_MULTIPLEXER = 2;
    @Override
    public List<OptimalPath> search(final SearchSettings settings)
            throws Exception {
        long st = System.currentTimeMillis();
        System.out.println("");
        LOG.info("#-bfs-#-#-#-#-#-#-# BFS_V1: start search #-#-#-#-#-#-#-#-#");
        double sLat = settings.getStartLat();
        double sLng = settings.getStartLon();
        double eLat = settings.getEndLat();
        double eLng = settings.getEndLon();
        List<OptimalPath> result = new CopyOnWriteArrayList<>();
        LOG.info("#-bfs-# start coord [" + sLat + ", " + sLng + "]");
        LOG.info("#-bfs-#   end coord [" + eLat + ", " + eLng + "]");
        Graph graph = graphConstructor.findGraph(settings.getProfileId());
        TransportProfile profile = graphConstructor
                .findProfile(settings.getProfileId());
        Set<BusStop> all = graphConstructor
                .findBusStopPathsMap(profile.getId()).keySet();
        LOG.info("#-bfs-# total bus stops [" + all.size() + "]");
        // find fixed count closer bus stops near start point
        List<BusStop> startBs = transportGeometry.findNearestBusStops(
                profile.getSearchLimitForPoints(),all, sLat, sLng);
        // find fixed count closer bus stops near end point
        List<BusStop> endBs = transportGeometry.findNearestBusStops(
                profile.getSearchLimitForPoints(), all, eLat, eLng);
        LOG.debug("#-bfs-# start area - bus stop size ["
                + startBs.size() + "]");
        LOG.debug("#-bfs-#   end area - bus stop size [" + endBs.size() + "]");
        // getting start vertices for search (start search conditions)
        Map<Integer, Set<BusStop>> endVertices = createPointVertices(
                endBs, false, profile, graph);
        // getting end vertices for search (end search conditions)
        Map<Integer, Set<BusStop>> startVertices = createPointVertices(
                startBs, true, profile, graph);
        LOG.info("#-bfs-# start vertices [" + startVertices.size() + "]");
        LOG.info("#-bfs-# end vertices [" + endVertices.size() + "]");
        // search straight paths, it's simple -)
        List<OptimalPath> straight = straightPaths(startVertices, endVertices,
                graph);
        if (!straight.isEmpty()) {
            LOG.info("#-bfs-# straight paths [" + straight.size() + "]");
            result.addAll(straight);
        }
        // exclude vertices which belong to both criteria (straight vertices)
        endVertices.keySet().forEach(v -> {
            if (startVertices.keySet().contains(v)) {
                LOG.debug("#-bfs-# exclude vertex from start criteria ["
                        + v + "], it exist in end criteria");
                startVertices.remove(v);
            }
        });
        // multi-threading, using [physical processors] or [physical processors]
        // + [hyper-threading]
        int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService ex = Executors.newFixedThreadPool(cores);
        // increase search depth
        long startBfs = System.currentTimeMillis();
        List<Future<List<OptimalPath>>> futures = new ArrayList<>();
        // break for threads
        List<Integer>[] portions = new ArrayList[cores];
        for (int i = 0; i < cores; i++) {
            portions[i] = new ArrayList<>();
        }
        Iterator<Integer> itr = startVertices.keySet().iterator();
        int counter = 0;
        while (itr.hasNext()) {
            portions[counter % cores].add(itr.next());
            counter++;
        }
        for (List<Integer> portion : portions) {
            futures.add(ex.submit(
                    new BFSTask(portion, endVertices, startVertices, graph,
                            settings.getMaxTransfers())
            ));
        }
        // getting results
        for (Future<List<OptimalPath>> f : futures) {
            result.addAll(f.get());
        }
        LOG.info("#-bfs-# total number of decisions ["
                + (result.size() - straight.size()) + "], BFS time ["
                + (System.currentTimeMillis() - startBfs) + "] ms");
        result = clearUnrealResults(result, startBs);
        List<OptimalPath>[] grouping = groupingResult(result, settings);
        result = grouping[0];
        sortResults(result);
        result = result.subList(0, settings.getMaxResults());
        LOG.info("#-bfs-# total number of optimal paths ["
                + result.size() + "]");
        LOG.info("#-bfs-# elapsed time [" + (System.currentTimeMillis() - st)
                + "]");
        LOG.info("#-bfs-#-#-#-#-#-#-#-#-#-# search complete #-#-#-#-#-#-#\n");
        return result;
    }
    @Override
    protected Map<Integer, Set<BusStop>> createPointVertices(
            final List<BusStop> pointBusStops, final boolean isStart,
            final TransportProfile profile, final Graph graph)
            throws Exception {
        // key - vertex number, value - set bus stops
        Map<Integer, Set<BusStop>> map = new HashMap<>();
        List<BusStop> way;
        Map<BusStop, List<Path>> bsPaths = graphConstructor
                .findBusStopPathsMap(profile.getId());
        for (BusStop bs : pointBusStops) {
            // getting bus stop paths
            List<Path> paths = bsPaths.get(bs);
            if (paths == null) {
                continue;
            }
            for (Path path : paths) {
                // getting path way
                way = path.getBusstops();
                if (isStart) {
                    // for start bus stops skip paths where start bus stop
                    // in the end of way
                    if (way.indexOf(bs) == way.size() - 1) {
                        continue;
                    }
                } else {
                    // for end bus stops skip paths where end bus stop
                    // in the start of way
                    if (way.indexOf(bs) == 0) {
                        continue;
                    }
                }
                // getting vertex number for path
                int idx = graph.indexOfPath(path);
                if (idx == -1) {
                    throw new IllegalArgumentException("vertex not defined for "
                            + path);
                }
                if (map.containsKey(idx)) {
                    map.get(idx).add(bs);
                } else {
                    Set<BusStop> l = new HashSet<>();
                    l.add(bs);
                    map.put(idx, l);
                }
            }
        }
        return map;
    }
    @Override
    protected List<OptimalPath> straightPaths(
            final Map<Integer, Set<BusStop>> startVertices,
            final Map<Integer, Set<BusStop>> endVertices,
            final Graph graph) throws Exception {
        List<BusStop> way;
        Path path;
        List<OptimalPath> list = new ArrayList<>();
        for (Integer s : startVertices.keySet()) {
            for (Integer e : endVertices.keySet()) {
                // vertex exist in start & end points - straight path
                if (s.intValue() == e.intValue()) {
                    // straight path found
                    path = graph.getPath(s);
                    way = path.getBusstops();
                    for (BusStop startBs : startVertices.get(s)) {
                        for (BusStop endBs : endVertices.get(e)) {
                            // only if start bus stop before end bus stop
                            if (way.indexOf(startBs) < way.indexOf(endBs)) {
                                List<List<BusStop>> opWay =
                                        new ArrayList<>();
                                List<Path> opPaths = new ArrayList<>();
                                opWay.add(way.subList(way.indexOf(startBs),
                                        way.indexOf(endBs) + 1));
                                opPaths.add(path);
                                OptimalPath op = new OptimalPath();
                                op.setPath(opPaths);
                                op.setWay(opWay);
                                list.add(op);
                            }
                        }
                    }
                }
            }
        }
        return list;
    }
    @Override
    protected void sortResults(final List<OptimalPath> result)
            throws Exception {
        Collections.sort(result, (OptimalPath o1, OptimalPath o2) -> {
            if (o1.getTime() > o2.getTime()) {
                return -1;
            } else if (o1.getTime() < o2.getTime()) {
                return 1;
            } else {
                return 0;
            }
        });
    }
    /**
     * Grouping result by time and distance.
     * @param dirty dirty result.
     * @param settings search settings.
     * @return result in two parts: short paths in under zero index,
     *      and long under 1 index.
     * @throws Exception 
     */
    private List<OptimalPath>[] groupingResult(final List<OptimalPath> dirty,
            final SearchSettings settings) throws Exception {
        long start = System.currentTimeMillis();
        double sLat = settings.getStartLat();
        double sLon = settings.getStartLon();
        double eLat = settings.getEndLat();
        double eLon = settings.getEndLon();
        long minInHour = TimeUnit.HOURS.toMinutes(1);
        List<OptimalPath> total = new ArrayList<>();
        Map<String, List<OptimalPath>> grouping = new HashMap<>();
        StringBuilder k = new StringBuilder();
        for (OptimalPath op : dirty) {
            k.setLength(0);
            for (Path p : op.getPath()) {
                k.append(p.getId()).append("#");
            }
            if (grouping.containsKey(k.toString())) {
                grouping.get(k.toString()).add(op);
            } else {
                List<OptimalPath> opList = new ArrayList<>();
                opList.add(op);
                grouping.put(k.toString(), opList);
            }
        }
        LOG.info("#-bfs-# dirty [" + dirty.size()
                + "], groups [" + grouping.size() + "]");
        double max = -1;
        double min = Double.MAX_VALUE;
        for (List<OptimalPath> list : grouping.values()) {
            OptimalPath best = selectBest(list, sLat, sLon, eLat, eLon);
            if (best.getTime() > max) {
                max = best.getTime();
            }
            if (best.getTime() < min) {
                min = best.getTime();
            }
            total.add(best);
        }
        double limitTime = min * LIMIT_TIME_MULTIPLEXER;
        LOG.info("#-bfs-# max time [" + (max * minInHour)
                + "] min");
        LOG.info("#-bfs-# min time [" + (min * minInHour)
                + "] min");
        LOG.info("#-bfs-# limit time [" + (limitTime * minInHour)
                + "] min");
        List<OptimalPath> longPaths = new ArrayList<>();
        List<OptimalPath> shortPaths = new ArrayList<>();
        for (OptimalPath op : total) {
            if (op.getTime() < limitTime) {
                shortPaths.add(op);
            } else {
                longPaths.add(op);
            }
        }
        LOG.info("#-bfs-# total paths size [" + total.size() + "]");
        LOG.info("#-bfs-# short paths size [" + shortPaths.size() + "]");
        LOG.info("#-bfs-# long paths size [" + longPaths.size() + "]");
        if (LOG.isDebugEnabled()) {
            Map<Integer, Integer> m = new HashMap<>();
            for (OptimalPath op : shortPaths) {
                double percent = op.getTime() * 100 / max;
                if (m.containsKey((int) percent)) {
                    m.put((int) percent, m.get((int) percent) + 1);
                } else {
                    m.put((int) percent, 1);
                }
            }
            List<Integer> ks = new ArrayList<>(m.keySet());
            Collections.sort(ks);
            if (LOG.isDebugEnabled()) {
                for (Integer p : ks) {
                    LOG.debug("#-bfs-#" + String.format("%-7s", m.get(p))
                            + String.format("%-26s", " [~"
                            + (p * max / 100) + "] ms") + " : "
                            + new String(new char[p]).replace("\0", "."));
                }
            }
        }
        LOG.info("#-bfs-# grouping elapsed time ["
                + (System.currentTimeMillis() - start) + "]");
        return new List[] {shortPaths, longPaths};
    }
    /**
     * Select best optimal path from same optimal paths.
     * @param ops - list optimal ways for same path.
     * @param sLat - start point latitude.
     * @param sLng - start point longitude.
     * @param eLat - end point latitude.
     * @param eLng - end point longitude.
     * @return - best optimal path.
     * @throws Exception - method error.
     */
    private OptimalPath selectBest(final List<OptimalPath> ops,
            final double sLat, final double sLng, final double eLat,
            final double eLng) throws Exception {
        OptimalPath best = null;
        for (OptimalPath op : ops) {
            BusStop firstBs = op.getWay().get(0).get(0);
            List<BusStop> lastSubWay = op.getWay()
                    .get(op.getWay().size() - 1);
            BusStop lastBs = lastSubWay.get(lastSubWay.size() - 1);
            double startDist = geometry.calcDistance(sLat, sLng,
                    firstBs.getLatitude(), firstBs.getLongitude());
            double endDist = geometry.calcDistance(eLat, eLng,
                    lastBs.getLatitude(), lastBs.getLongitude());
            double totalTime = (startDist + endDist)
                    / TransportConst.HUMAN_SPEED
                    + transportGeometry.calcOptimalPathTime(op);
            op.setTime(totalTime);
            if (best == null) {
                best = op;
            } else {
                if (best.getTime() > totalTime) {
                    best = op;
                }
            }
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("#-bfs-# best --> " + best);
        }
        return best;
    }
}
