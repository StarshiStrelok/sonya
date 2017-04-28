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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import ss.sonya.entity.BusStop;
import ss.sonya.entity.Path;
import ss.sonya.entity.Route;
import ss.sonya.entity.TransportProfile;
import ss.sonya.transport.search.vo.Decision;
import ss.sonya.transport.search.vo.OptimalPath;
import ss.sonya.transport.search.vo.SearchSettings;

/**
 * Search engine, based on breadth-first search algorithm for graphs.
 * @author ss
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class BFSAlgorithmV1 extends BFS implements SearchEngine {
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
        LOG.debug("#-bfs-# start vertices [" + startVertices.size() + "]");
        LOG.debug("#-bfs-# end vertices [" + endVertices.size() + "]");
        // search straight paths, it's simple -)
        List<OptimalPath> straight = straightPaths(startVertices, endVertices,
                graph);
        if (!straight.isEmpty()) {
            LOG.debug("#-bfs-# straight paths [" + straight.size() + "]");
            result.addAll(straight);
        }
        // multi-threading, using [physical processors] or [physical processors]
        // + [hyper-threading]
        int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService ex = Executors.newFixedThreadPool(cores);
        List<Decision> decisions = new CopyOnWriteArrayList<>();
        // increase search depth
        long startBfs = System.currentTimeMillis();
        List<Future<List<Decision>>> futures = new ArrayList<>();
        // for every start condition create separate thread
        for (int startIdx : startVertices.keySet()) {
            futures.add(ex.submit(
                    new BFSTask(startIdx, endVertices,
                        startVertices.get(startIdx), graph)
            ));
        }
        // getting results
        for (Future<List<Decision>> f : futures) {
            decisions.addAll(f.get());
        }
        LOG.debug("#-bfs-# phase end #-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#");
        LOG.info("#-bfs-# total number of decisions ["
                + decisions.size() + "], BFS time ["
                + (System.currentTimeMillis() - startBfs) + "] ms");
        // multithreading
        long start = System.currentTimeMillis();
        int portionSize = decisions.size() / cores;
        LOG.info("#-bfs-# portion size [" + portionSize + "]");
        List<Future<List<OptimalPath>>> tasks = new ArrayList<>(cores);
        int min, max;
        for (int i = 1; i <= cores; i++) {
            min = (i - 1) * portionSize;
            if ((i + 1) * portionSize < decisions.size()) {
                max = i * portionSize;
            } else {
                max = decisions.size();
            }
            tasks.add(ex.submit(
                    new TransformDecisionsTask(
                            decisions.subList(min, max), graph)
            ));
        }
        for (Future<List<OptimalPath>> task : tasks) {
            result.addAll(task.get());
        }
        LOG.info("#-bfs-# transform decisions elapsed time ["
                + (System.currentTimeMillis() - start) + "] ms");
        clearUnrealResults(result, startBs);
        startVertices.clear();
        endVertices.clear();
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
        Route route;
        Map<BusStop, List<Path>> bsPaths = graphConstructor
                .findBusStopPathsMap(profile.getId());
        for (BusStop bs : pointBusStops) {
            // getting bus stop paths
            List<Path> paths = bsPaths.get(bs);
            if (paths == null) {
                continue;
            }
            for (Path path : paths) {
                // getting path route
                route = path.getRoute();
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
    protected List<OptimalPath> transformDecisions(
            List<Decision> decisions, Graph graph) throws Exception {
        return Collections.emptyList();
    }
}
