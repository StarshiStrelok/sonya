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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ss.sonya.transport.constants.TransportConst;
import ss.sonya.entity.BusStop;
import ss.sonya.entity.Path;
import ss.sonya.entity.Route;
import ss.sonya.entity.RouteProfile;
import ss.sonya.entity.TransportProfile;
import ss.sonya.entity.Trip;
import ss.sonya.inject.DataService;
import ss.sonya.inject.service.Geometry;
import ss.sonya.transport.api.TransportDataService;
import ss.sonya.transport.component.TransportGeometry;
import ss.sonya.transport.component.VirtualScheduleBuilder;

/**
 * Graph constructor.
 * Build all graphs from transport profiles.
 * @author ss
 */
@Service
public class GraphConstructor {
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(GraphConstructor.class);
    /** First path transfer, from bus stop (path#1). */
    private static final int TRANSFER_1_FROM = 0;
    /** First path transfer, to bus stop (path#2). */
    private static final int TRANSFER_1_TO = 1;
    /** Last path transfer, from bus stop (path#1). */
    private static final int TRANSFER_2_FROM = 2;
    /** Last path transfer, to bus stop (path#2). */
    private static final int TRANSFER_2_TO = 3;
    /** All graphs, key - transport profile ID. */
    private static final Map<Integer, Graph> GRAPHS = new ConcurrentHashMap<>();
    /** Transport profiles map. */
    private static final Map<Integer, TransportProfile> PROFILES =
            new ConcurrentHashMap<>();
    /** Bus stop paths cache. */
    private static final Map<Integer, Map<BusStop, List<Path>>> BUS_STOP_PATHS =
            new HashMap<>();
    /** Data service. */
    @Autowired
    private DataService dataService;
    /** Transport service. */
    @Autowired
    private TransportDataService transportService;
    /** Transport geometry. */
    @Autowired
    private TransportGeometry transportGeometry;
    /** Geometry. */
    @Autowired
    private Geometry geometry;
    /** Virtual schedule builder. */
    @Autowired
    private VirtualScheduleBuilder virtualScheduleBuilder;
    /** Initialization. */
    @PostConstruct
    public void init() {
        LOG.info("======================= GRAPHS INITIALIZATION =============");
        try {
            List<TransportProfile> profiles = dataService
                    .getAll(TransportProfile.class);
            profiles.stream().forEach(profile -> {
                try {
                    GRAPHS.put(profile.getId(), buildGraph(profile));
                    PROFILES.put(profile.getId(), profile);
                    LOG.info("================================================"
                            + "===========");
                } catch (Exception ex) {
                    LOG.fatal("build graph error! " + profile, ex);
                }
            });
        } catch (Exception e) {
            LOG.fatal("init graph constructor error!", e);
        }
        LOG.info("======================= COMPLETE ==========================");
    }
    /**
     * Find graph.
     * @param profileId transport profile ID.
     * @return graph.
     */
    public Graph findGraph(final Integer profileId) {
        return GRAPHS.get(profileId);
    }
    /**
     * Find transport profile.
     * @param profileId transport profile ID.
     * @return transport profile.
     */
    public TransportProfile findProfile(final Integer profileId) {
        return PROFILES.get(profileId);
    }
    /**
     * Find bus stop paths map.
     * @param profileId transport profile ID.
     * @return map.
     */
    public Map<BusStop, List<Path>> findBusStopPathsMap(
            final Integer profileId) {
        return BUS_STOP_PATHS.get(profileId);
    }
    /**
     * Build graph for one transport profile.
     * @param profile transport profile.
     * @return graph.
     * @throws Exception error.
     */
    public Graph buildGraph(final TransportProfile profile) throws Exception {
        LOG.info("--------------- GRAPH (" + profile + ") -------------------");
        long start = System.currentTimeMillis();
        List<Path> paths = transportService
                .getFromProfile(profile.getId(), Path.class);
        // sort very important, path vertex number will
        // correspond path in sorted array
        Collections.sort(paths,
                (Path o1, Path o2) -> o1.getId() > o2.getId() ? 1 : -1);
        LOG.info("paths count [" + paths.size() + "]");
        List<BusStop> all = transportService
                .getFromProfile(profile.getId(), BusStop.class);
        LOG.info("bus stops count [" + all.size() + "]");
        Graph graph = new Graph(paths);
        Map<BusStop, List<Path>> bsPaths = new HashMap<>();
        paths.stream().forEach(path -> {
            path.getBusstops().stream().forEach(bs -> {
                if (bsPaths.containsKey(bs)) {
                    bsPaths.get(bs).add(path);
                } else {
                    List<Path> l = new LinkedList<>();
                    l.add(path);
                    bsPaths.put(bs, l);
                }
            });
        });
        BUS_STOP_PATHS.put(profile.getId(), bsPaths);
        // for search transfer paths required found closest bus stops for
        // every bus stop in path way, cache using for speed up
        Map<BusStop, List<BusStop>> nearBsCache = new HashMap<>();
        // For every path search transfer paths and create edges
        BusStop bs;
        BusStop transferBs;
        List<BusStop> way;
        for (Path path : paths) {
            int vertex = paths.indexOf(path);
            if (TransportConst.METRO
                    .equals(path.getRoute().getType().getName())) {
                graph.addMetroVertex(vertex);
            }
            way = path.getBusstops();
            // getting transfer paths for current path
            Map<Path, BusStop[]> tMap = analyzePath(path, nearBsCache, bsPaths,
                    all, profile.getBusStopAccessZoneRadius());
            // for every transfer path create edge
            // and add it to current path vertex
            for (Path transferPath : tMap.keySet()) {
                // getting [path] - [transfer path] transfer bus stops
                BusStop[] pairs = tMap.get(transferPath);
                int[] tInfo = new int[pairs.length];
                int[] rtInfo = new int[pairs.length];   // reverse transfers
                // transfer from path to path
                int tPathVertex = paths.indexOf(transferPath);
                for (int i = 0; i < pairs.length; i += 2) {
                    bs = pairs[i];
                    transferBs = pairs[i + 1];
                    int pathBsOrder = way.indexOf(bs);
                    int tPathBsOrder = transferPath.getBusstops()
                            .indexOf(transferBs);
                    tInfo[i] = pathBsOrder;
                    tInfo[i + 1] = tPathBsOrder;
                    rtInfo[i] = tPathBsOrder;
                    rtInfo[i + 1] = pathBsOrder;
                }
                graph.addEdge(vertex, tPathVertex, tInfo);
                graph.addEdge(tPathVertex, vertex, rtInfo);
            }
        }
        LOG.info("--- " + graph.toString());       // output graph
        if (profile.isHasSchedule()) {
            long startSchedule = System.currentTimeMillis();
            for (Path p : paths) {
                List<Trip> schedule = transportService.getSchedule(p.getId());
                if (schedule.get(0) != null
                        && schedule.get(0).getIrregular() != null
                        && !schedule.get(0).getIrregular().isEmpty()) {
                    List<BusStop> fullWay = p.getBusstops();
                    RouteProfile type = p.getRoute().getType();
                    schedule = virtualScheduleBuilder
                            .buildVirtualSchedule(schedule, fullWay, type);
                }
                graph.putSchedule(p, buildSchedule(schedule));
            }
            LOG.info("build schedule for graph, elapsed time ["
                    + (System.currentTimeMillis() - startSchedule) + "] ms");
        }
        LOG.info("--- build path graph end... Elapsed time ["
                + (System.currentTimeMillis() - start) + "] ms");
        return graph;
    }
    /**
     * Analyze path.
     * @param path current analyzed path.
     * @param accessZoneBsCache cache for access zones.
     * @param bsPaths map, contains bus stop and paths, passing through it.
     * @param all all bus stops.
     * @param radius access zone radius for bus stop.
     * @return transfer map, key - transfer bus stop, value - bus stops.
     * @throws Exception error.
     */
    private Map<Path, BusStop[]> analyzePath(final Path path,
            final Map<BusStop, List<BusStop>> accessZoneBsCache,
            final Map<BusStop, List<Path>> bsPaths,
            final List<BusStop> all, final double radius) throws Exception {
        Map<Path, BusStop[]> transferMap = new HashMap<>();
        List<BusStop> way = path.getBusstops();
        for (int i = 0; i < way.size(); i++) {
            if (i == 0) {
                continue;
            }
            // current way bus stop
            BusStop bs = way.get(i);
            if (TransportConst.MOCK_BS.equals(bs.getName())) {
                continue;
            }
            // getting bus stops in access zone
            List<BusStop> nears = accessZoneBusstops(
                    bs, all, accessZoneBsCache, radius);
            // every from closer bus stops has many paths passing through
            for (BusStop transferBs : nears) {
                // for every from transfer paths
                List<Path> transferPaths = bsPaths.get(transferBs);
                if (transferPaths == null) {
                    continue;
                }
                for (Path transferPath : transferPaths) {
                    // same route skip always
                    Route transferRoute = transferPath.getRoute();
                    if (path.getRoute().getId().equals(transferRoute.getId())) {
                        continue;
                    }
                    // insert in result
                    if (transferMap.containsKey(transferPath)) {
                        BusStop[] pairs = transferMap.get(transferPath);
                        // always add all transfers if one of paths is metro
                        if (TransportConst.METRO.equals(transferPath.getRoute()
                                .getType().getName())
                                || TransportConst.METRO.equals(
                                        path.getRoute().getType().getName())) {
                            List<BusStop> tmpList = new ArrayList<>(
                                    Arrays.asList(pairs));
                            tmpList.add(bs);
                            tmpList.add(transferBs);
                            pairs = tmpList.toArray(new BusStop[0]);
                            transferMap.put(transferPath, pairs);
                            continue;
                        }
                        // else add 2 transfers max for other transport types
                        if (bs.equals(pairs[TRANSFER_1_FROM])) {
                            // find closest transfer bus stop
                            // for first transfer
                            if (isClosest(
                                    bs, pairs[TRANSFER_1_TO], transferBs)) {
                                pairs[TRANSFER_1_TO] = transferBs;
                                transferMap.put(transferPath, pairs);
                            }
                        } else if (Math.abs(way.indexOf(bs)
                                - way.indexOf(pairs[TRANSFER_1_FROM])) == 1) {
                            // select shortest transfer
                            // if bus stop each other
                            double d1 = geometry.calcDistance(
                                    pairs[TRANSFER_1_FROM].getLatitude(),
                                    pairs[TRANSFER_1_FROM].getLongitude(),
                                    pairs[TRANSFER_1_TO].getLatitude(),
                                    pairs[TRANSFER_1_TO].getLongitude()
                            );
                            double d2 = geometry.calcDistance(
                                    bs.getLatitude(),
                                    bs.getLongitude(),
                                    transferBs.getLatitude(),
                                    transferBs.getLongitude()
                            );
                            if (d2 < d1) {
                                pairs[TRANSFER_1_FROM] = bs;
                                pairs[TRANSFER_1_TO] = transferBs;
                            }
                        } else {                    // last transfer
                            if (pairs.length == 2) {
                                pairs = new BusStop[] {pairs[0], pairs[1],
                                    null, null};
                            }
                            if (pairs[TRANSFER_2_FROM] == null) {
                                pairs[TRANSFER_2_FROM] = bs;
                                pairs[TRANSFER_2_TO] = transferBs;
                            } else if (Math.abs(way.indexOf(bs) - way.indexOf(
                                            pairs[TRANSFER_2_FROM])) == 1) {
                                // select shortest transfer
                                // if bus stop each other
                                double d1 = geometry.calcDistance(
                                        pairs[TRANSFER_2_FROM].getLatitude(),
                                        pairs[TRANSFER_2_FROM].getLongitude(),
                                        pairs[TRANSFER_2_TO].getLatitude(),
                                        pairs[TRANSFER_2_TO].getLongitude()
                                );
                                double d2 = geometry.calcDistance(
                                        bs.getLatitude(),
                                        bs.getLongitude(),
                                        transferBs.getLatitude(),
                                        transferBs.getLongitude()
                                );
                                if (d2 < d1) {
                                    pairs[TRANSFER_2_FROM] = bs;
                                    pairs[TRANSFER_2_TO] = transferBs;
                                }
                            } else {
                                List<BusStop> tWay = transferPath.getBusstops();
                                if (tWay.indexOf(pairs[TRANSFER_2_TO])
                                        < tWay.indexOf(transferBs)) {
                                    pairs[TRANSFER_2_FROM] = bs;
                                    pairs[TRANSFER_2_TO] = transferBs;
                                }
                            }
                        }
                        transferMap.put(transferPath, pairs);
                    } else {
                        // 1-element: current path bus stop
                        // 2-element: first transfer bus stop
                        // 3-element: current path bus stop
                        // 4-element: last transfer bus stop
                        transferMap.put(transferPath, new BusStop[] {
                            bs, transferBs
                        });
                    }
                }
            }
        }
        if (!TransportConst.METRO.equals(path.getRoute().getType().getName())) {
            mergeNeighboringTransfers(way, transferMap);
        }
        return transferMap;
    }
    /**
     * Merge neighboring transfers.
     * Select shortest only.
     * @param way path way.
     * @param transferMap transfer map.
     * @throws Exception error.
     */
    private void mergeNeighboringTransfers(final List<BusStop> way,
            final Map<Path, BusStop[]> transferMap) throws Exception {
        // if start & end transfer follow each other - select best
        for (Path tPath : transferMap.keySet()) {
            if (TransportConst.METRO.equals(
                    tPath.getRoute().getType().getName())) {
                continue;
            }
            BusStop[] pairs = transferMap.get(tPath);
            if (pairs.length > 2 && pairs[TRANSFER_1_FROM] != null
                    && pairs[TRANSFER_2_FROM] != null) {
                int diff = way.indexOf(pairs[TRANSFER_1_FROM])
                        - way.indexOf(pairs[TRANSFER_2_FROM]);
                if (Math.abs(diff) == 1) {
                    double d1 = geometry.calcDistance(
                            pairs[TRANSFER_1_FROM].getLatitude(),
                            pairs[TRANSFER_1_FROM].getLongitude(),
                            pairs[TRANSFER_1_TO].getLatitude(),
                            pairs[TRANSFER_1_TO].getLongitude());
                    double d2 = geometry.calcDistance(
                            pairs[TRANSFER_2_FROM].getLatitude(),
                            pairs[TRANSFER_2_FROM].getLongitude(),
                            pairs[TRANSFER_2_TO].getLatitude(),
                            pairs[TRANSFER_2_TO].getLongitude());
                    // clear second transfer
                    pairs[TRANSFER_1_FROM] = d1 < d2 ? pairs[TRANSFER_1_FROM]
                            : pairs[TRANSFER_2_FROM];
                    pairs[TRANSFER_1_TO] = d1 < d2 ? pairs[TRANSFER_1_TO]
                            : pairs[TRANSFER_2_TO];
                    pairs = new BusStop[] {
                        pairs[TRANSFER_1_FROM],
                        pairs[TRANSFER_1_TO]
                    };
                    transferMap.put(tPath, pairs);
                }
            }
        }
    }
    /**
     * Get transfer bus stops from access zone for current bus stop.
     * @param bs current bus stop.
     * @param allBs all bus stops.
     * @param zoneBsCache access zone cache.
     * @param radius access zone radius.
     * @return list bus stops from access zone.
     */
    private List<BusStop> accessZoneBusstops(final BusStop bs,
            final List<BusStop> allBs,
            final Map<BusStop, List<BusStop>> zoneBsCache,
            final double radius) {
        if (zoneBsCache.containsKey(bs)) {
            return zoneBsCache.get(bs);
        }
        List<BusStop> zoneBs = transportGeometry
                .findBusStopsInRadius(allBs, bs, radius);
        zoneBs.add(bs);
        zoneBsCache.put(bs, zoneBs);
        return zoneBs;
    }
    /**
     * Check if new transfer bus stop closer then old transfer.
     * @param bs bus stop.
     * @param oldT old transfer bus stop.
     * @param newT new transfer bus stop.
     * @return true if closer.
     */
    private boolean isClosest(final BusStop bs, final BusStop oldT,
            final BusStop newT) {
        double dist1 = geometry.calcDistance(
                bs.getLatitude(), bs.getLongitude(),
                oldT.getLatitude(), oldT.getLongitude());
        double dist2 = geometry.calcDistance(bs.getLatitude(),
                bs.getLongitude(),
                newT.getLatitude(), newT.getLongitude());
        return dist1 > dist2;
    }
    /**
     * Parse schedule data and create object model.
     * @param schedule - path schedule.
     * @return - object model <days, times>.
     * @throws Exception - operation error.
     */
    private Map<String, List<List<String>>> buildSchedule(
            final List<Trip> schedule) throws Exception {
        Map<String, List<List<String>>> tripMap = new HashMap<>();
        for (Trip trip : schedule) {
            String days = trip.getDays();
            String[] arr = trip.getRegular().split(",", -1);
            List<String> lInf = Arrays.asList(arr);
            if (tripMap.containsKey(days)) {
                tripMap.get(days).add(lInf);
            } else {
                List<List<String>> l = new ArrayList<>();
                l.add(lInf);
                tripMap.put(days, l);
            }
        }
        return tripMap;
    }
}

