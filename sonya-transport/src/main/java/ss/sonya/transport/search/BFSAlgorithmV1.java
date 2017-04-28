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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import ss.sonya.entity.BusStop;
import ss.sonya.entity.RouteProfile;
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
        List<BusStop> all = new ArrayList<>();
        Set<BusStop> bsSet = new HashSet<>();
        // !!! must be more speed for this code, do later
        graph.getPaths().stream().forEach(path -> {
            if (settings.getTypes().contains(path.getRoute().getType())) {
                bsSet.addAll(path.getBusstops());
            }
        });
        all.addAll(bsSet);
        bsSet.clear();
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
        Map<Integer, Set<BusStop>> endVertices = createEndpoints(
                endBs, false, settings.getTypes());
        // getting end vertices for search (end search conditions)
        Map<Integer, Set<BusStop>> startVertices = createEndpoints(
                startBs, true, settings.getTypes());
        LOG.debug("#-bfs-# start vertices [" + startVertices.size() + "]");
        LOG.debug("#-bfs-# end vertices [" + endVertices.size() + "]");
        // search straight paths, it's simple -)
        List<OptimalPath> straight = straightPaths(startVertices, endVertices,
                settings.getTypes());
        if (!straight.isEmpty()) {
            LOG.debug("#-bfs-# straight paths [" + straight.size() + "]");
            result.addAll(straight);
        }
        // create cache for search speed-up
        // key - start vertex number, value - graph, broken by depth
        Map<Integer, Map<Integer, Set<Integer>>> graphCache =
                new ConcurrentHashMap<>();
        // multi-threading, using [physical processors] or [physical processors]
        // + [hyper-threading], or predefined setting
        int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService ex = Executors.newFixedThreadPool(cores);
        // break by depth operation
        long startBreak = System.currentTimeMillis();
        List<Future<Void>> breakTasks = new ArrayList<>();
        for (int startIdx : startVertices.keySet()) {
            breakTasks.add(ex.submit(new BreakGraphTask(
                    graphCache, settings.getTypes(), startIdx, graph)));
        }
        for (Future<Void> task : breakTasks) {
            task.get();
        }
        LOG.info("#-bfs-# break graph time ["
                + (System.currentTimeMillis() - startBreak) + "] ms");
        Integer limitDepth = 1;
        List<Decision> decisions = new CopyOnWriteArrayList<>();
        // increase search depth
        long startBfs = System.currentTimeMillis();
        while (limitDepth <= settings.getMaxTransfers()
                || (decisions.isEmpty() && straight.isEmpty())) {
            LOG.debug("#-bfs-# current depth [" + limitDepth + "] #-#-#-#-#-#");
            List<Future<List<Decision>>> futures = new ArrayList<>();
            // for every start condition create separate thread
            for (int startIdx : startVertices.keySet()) {
                futures.add(ex.submit(new BFSTask(graphCache.get(startIdx),
                        startIdx, limitDepth, endVertices, startVertices,
                        graph)));
            }
            // getting results
            for (Future<List<Decision>> f : futures) {
                decisions.addAll(f.get());
            }
            LOG.debug("#-bfs-# phase end #-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#");
            limitDepth++;
        }
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
                    new TransformDecisionsTask(decisions.subList(min, max))
            ));
        }
        for (Future<List<OptimalPath>> task : tasks) {
            result.addAll(task.get());
        }
        LOG.info("#-bfs-# transform decisions elapsed time ["
                + (System.currentTimeMillis() - start) + "] ms");
        clearUnrealResults(result, startBs);
        graphCache.clear();
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
    protected Map<Integer, Set<BusStop>> createEndpoints(List<BusStop> bsList, boolean isStart, List<RouteProfile> types) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    protected List<OptimalPath> transformDecisions(List<Decision> decisions) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    protected List<OptimalPath> straightPaths(Map<Integer, Set<BusStop>> startVertices, Map<Integer, Set<BusStop>> endVertices, List<RouteProfile> types) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
