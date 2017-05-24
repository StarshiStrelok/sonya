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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import ss.sonya.constants.TransportConst;
import ss.sonya.entity.BusStop;
import ss.sonya.entity.Path;
import ss.sonya.entity.TransportProfile;
import ss.sonya.inject.service.Geometry;
import ss.sonya.transport.component.TransportGeometry;
import ss.sonya.transport.search.vo.BusStopTime;
import ss.sonya.transport.search.vo.OptimalPath;
import ss.sonya.transport.search.vo.OptimalSchedule;
import ss.sonya.transport.search.vo.SearchSettings;

/**
 * Search engine, based on breadth-first search algorithm for graphs.
 * @author ss
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class BFSAlgorithmV1 implements SearchEngine {
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(BFSAlgorithmV1.class);
    /** Limit time multiplexer. max_time=min_time * multiplexer. */
    private static final double LIMIT_TIME_MULTIPLEXER = 2;
    /** Transport geometry. */
    @Autowired
    private TransportGeometry transportGeometry;
    /** Geometry. */
    @Autowired
    private Geometry geometry;
    /** Graph constructor. */
    @Autowired
    private GraphConstructor graphConstructor;
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
        LOG.info("#-bfs-# profile [" + profile + "]");
        Set<BusStop> all = graphConstructor
                .findBusStopPathsMap(profile.getId()).keySet();
        LOG.info("#-bfs-# total bus stops [" + all.size() + "]");
        // find fixed count closer bus stops near start point
        List<BusStop> startBs = transportGeometry.findNearestBusStops(
                profile.getSearchLimitForPoints(), all, sLat, sLng);
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
        if (settings.getMaxTransfers() > 0) {
            // exclude vertices which belong to both criteria (straight vert.)
            endVertices.keySet().forEach(v -> {
                if (startVertices.keySet().contains(v)) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("#-bfs-# exclude vertex from start criteria ["
                                + v + "], it exist in end criteria");
                    }
                    startVertices.remove(v);
                }
            });
            // multi-threading, using [physical processors]
            // or [physical processors] + [hyper-threading]
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
        }
        // at this moment result not thread safe
        result = clearUnrealResults(result, startBs);
        List<OptimalPath>[] grouping = groupingResult(result, settings);
        result = grouping[0];
        sortResults(result, settings, profile);
        if (result.size() > settings.getMaxResults()) {
            result = result.subList(0, settings.getMaxResults());
        }
        LOG.info("#-bfs-# total number of optimal paths ["
                + result.size() + "]");
        LOG.info("#-bfs-# elapsed time [" + (System.currentTimeMillis() - st)
                + "]");
        LOG.info("#-bfs-#-#-#-#-#-#-#-#-#-# search complete #-#-#-#-#-#-#\n");
        return result;
    }
    /**
     * Create vertices for start or end vertices.
     * Grouping start / end bus stops by vertices (paths),
     * because every bus stop has paths, passing through it.
     * @param pointBusStops point bus stops.
     * @param isStart start or end point.
     * @param profile transport profile.
     * @param graph graph.
     * @return point vertices map.
     * @throws Exception - method error.
     */
    private Map<Integer, Set<BusStop>> createPointVertices(
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
    /**
     * Find straight paths.
     * @param startVertices start vertices.
     * @param endVertices end vertices.
     * @param graph graph.
     * @return straight paths list.
     * @throws Exception method error.
     */
    private List<OptimalPath> straightPaths(
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
    /**
     * Sort result.
     * @param result result.
     * @param settings search settings.
     * @param profile transport profile.
     * @throws Exception error.
     */
    protected void sortResults(final List<OptimalPath> result,
            final SearchSettings settings, final TransportProfile profile)
            throws Exception {
        if (profile.isHasSchedule()) {
            Graph graph = graphConstructor.findGraph(settings.getProfileId());
            insertSchedule(result, settings.getTime(), settings.getDay(),
                    graph);
            List<OptimalPath> withSchedule = new ArrayList<>();
            result.stream().forEach(op -> {
                if (op.getSchedule() != null) {
                    withSchedule.add(op);
                }
            });
            result.clear();
            result.addAll(withSchedule);
            Collections.sort(result, (OptimalPath o1, OptimalPath o2) -> {
                Date d1 = o1.getSchedule().getArrivalDate();
                Date d2 = o2.getSchedule().getArrivalDate();
                if (d1.getTime() < d2.getTime()) {
                    return -1;
                } else if (d1.getTime() > d2.getTime()) {
                    return 1;
                } else {
                    if (o1.getPath().size() > o2.getPath().size()) {
                        return 1;
                    } else if (o1.getPath().size() < o2.getPath().size()) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });
        } else {
            // move decisions with minimal transfers to top,
            // then sort it's by distance
            Collections.sort(result, (OptimalPath o1, OptimalPath o2) -> {
                if (o1.getPath().size() > o2.getPath().size()) {
                    return 1;
                } else if (o1.getPath().size() < o2.getPath().size()) {
                    return -1;
                } else {
                    if (o1.getTime() > o2.getTime()) {
                        return 1;
                    } else if (o1.getTime() < o2.getTime()) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });
        }
    }
    /**
     * Grouping result by time and distance.
     * @param dirty dirty result.
     * @param settings search settings.
     * @return result in two parts: short paths in under zero index,
     *      and long under 1 index.
     * @throws Exception error.
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
            transportGeometry.calcOptimalPathTime(op);
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
                    + op.getTime();
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
    /**
     * Insert schedule into optimal path.
     * @param opList optimal path list.
     * @param time start trip time.
     * @param day trip day.
     * @param graph graph.
     */
    private void insertSchedule(final List<OptimalPath> opList,
            final String time, final int day, final Graph graph) {
        long start = System.currentTimeMillis();
        int cores = Runtime.getRuntime().availableProcessors();
        int portionSize = opList.size() / cores;
        LOG.info("#-bfs-# portion size [" + portionSize + "]");
        List<Future<Void>> tasks = new ArrayList<>(cores);
        ExecutorService ex = Executors.newFixedThreadPool(cores);
        if (portionSize == 0) {
            tasks.add(ex.submit(
                    new InsertScheduleTask(opList, time, day, graph)
            ));
        } else {
            int min, max;
            for (int i = 1; i <= cores; i++) {
                min = (i - 1) * portionSize;
                if ((i + 1) * portionSize < opList.size()) {
                    max = i * portionSize;
                } else {
                    max = opList.size();
                }
                tasks.add(ex.submit(
                        new InsertScheduleTask(
                                opList.subList(min, max), time, day, graph)
                ));
            }
        }
        for (Future<Void> task : tasks) {
            try {
                task.get();
            } catch (InterruptedException | ExecutionException ex1) {
                LOG.error("insert schedule task error!", ex1);
            }
        }
        LOG.info("#-bfs-# insert schedule elapsed time ["
                + (System.currentTimeMillis() - start) + "] ms");
    }
        /**
     * Clear unreal results.
     * @param dirty dirty optimal paths.
     * @param startBs all start bus stops.
     * @return real optimal paths.
     * @throws Exception method error.
     */
    private List<OptimalPath> clearUnrealResults(
            final List<OptimalPath> dirty,
            final List<BusStop> startBs) throws Exception {
        long start = System.currentTimeMillis();
        List<OptimalPath> rest = new ArrayList<>();
        for (OptimalPath op : dirty) {
            if (op.getWay().size() > 1) {
                // if next way start bus stop in start area zone - remove it
                if (!startBs.contains(op.getWay().get(1).get(0))) {
                    rest.add(op);
                }
            } else {
                rest.add(op);
            }
//            for (List<BusStop> way : op.getWay()) {
//                if (way.size() <= 2 && op.getPath().get(
//                        op.getWay().indexOf(way))
//                        .getRoute().getType() != RouteType.METRO) {
//                    unreal.add(op);
//                    break;
//                }
//            }
        }
        LOG.info("#-bfs-# unreal results, was [" + dirty.size()
                    + "], rest [" + rest.size() + "], elapsed time ["
                    + (System.currentTimeMillis() - start) + "] ms");
        return rest;
    }
    /**
     * Insert schedule into optimal path.
     */
    private class InsertScheduleTask implements Callable<Void> {
        /** Portion of total list. */
        private final List<OptimalPath> portion;
        /** Start trip time. */
        private final String time;
        /** Trip day. */
        private final int day;
        /** Graph. */
        private final Graph graph;
        /** Time format. */
        private final SimpleDateFormat hhMM = new SimpleDateFormat("HH:mm");
        /**
         * Constructor.
         * @param p portion of total optimal path list.
         */
        InsertScheduleTask(final List<OptimalPath> p, final String pTime,
                final int pDay, final Graph g) {
            portion = p;
            time = pTime;
            day = pDay;
            graph = g;
        }
        @Override
        public Void call() throws Exception {
            for (OptimalPath op : portion) {
                op.setSchedule(buildOptimalSchedule(op));
            }
            return null;
        }
        /**
         * Put optimal schedule.
         * @param op - optimal schedule.
         * @return - optimal schedule or null.
         * @throws Exception - method error.
         */
        private OptimalSchedule buildOptimalSchedule(final OptimalPath op)
                throws Exception {
            OptimalSchedule os = new OptimalSchedule();
            List<BusStopTime[]> data = new ArrayList<>();
            Calendar c = GregorianCalendar.getInstance();
            Date startTrip = hhMM.parse(time);
            c.setTime(startTrip);
            int i = 0;
            Date cDate = null;
            Iterator<Path> itr = op.getPath().iterator();
            BusStop prevBs = null;
            String dayOfWeek = "" + day;
            long epoch = hhMM.parse("00:00").getTime();
            while (itr.hasNext()) {
                Path path = itr.next();
                Map<String, List<List<String>>> tripMap = graph
                        .getSchedule(path);
                List<List<String>> trips = null;
                if (tripMap == null) {
                    LOG.warn("Schedule absent for " + path);
                    return null;
                }
                for (String days : tripMap.keySet()) {
                    if (days.contains(dayOfWeek)) {
                        trips = tripMap.get(days);
                        break;
                    }
                }
                if (trips == null) {
                    return null;         // no trips today
                }
                List<BusStop> way = op.getWay().get(i);
                BusStop sBs = way.get(0);
                BusStop eBs = way.get(way.size() - 1);
                List<BusStop> fullWay = new ArrayList<>();
                for (BusStop bs : path.getBusstops()) {
                    if (!TransportConst.MOCK_BS.equals(bs.getName())) {
                        fullWay.add(bs);
                    }
                }
                int sIdx = fullWay.indexOf(sBs);
                int eIdx = fullWay.indexOf(eBs);
                Date nowDate = cDate == null ? startTrip : cDate;   // now date
                if (prevBs != null && !prevBs.equals(sBs)) {
                    // calc transfet distance.
                    double humanDist = geometry.calcDistance(prevBs.getLatitude(),
                            prevBs.getLongitude(), sBs.getLatitude(),
                            sBs.getLongitude());        // km
                    double addTime = humanDist
                            / TransportConst.HUMAN_SPEED;
                    double addTimeSec = addTime * TimeUnit.HOURS.toMinutes(1)
                            * TimeUnit.MINUTES.toSeconds(1);      // sec
                    nowDate = new Date(nowDate.getTime()
                            + (long) (addTimeSec * TimeUnit.SECONDS.toMillis(1)));
                }
                Date sDate = null;                              // start bs date
                List<String> foundTrip = null;                  // found trip
                Date tDate;                                     // temp variable
                for (int j = 0; j < trips.size(); j++) {
                    List<String> trip = trips.get(j);
                    String tm = trip.get(sIdx);
                    if (tm.isEmpty()) {
                        continue;
                    }
                    tDate = new Date(TransportConst.ALL_TIMES.get(tm));
                    // add day for times after 00:00
                    // and before transport midnight
                    if (tDate.getTime() < TransportConst.TRANSPORT_MIDNIGHT) {
                        tDate = new Date(tDate.getTime()
                                + TimeUnit.DAYS.toMillis(1));
                    }
                    if (tDate.after(nowDate)) {
                        if (sDate == null) {
                            sDate = tDate;
                            foundTrip = trip;
                        } else if (tDate.before(sDate)) {
                            sDate = tDate;
                            foundTrip = trip;
                        }
                    }
                }
                if (sDate == null || foundTrip == null) {
                    if (LOG.isTraceEnabled()) {
                        LOG.trace(path + " for [" + hhMM.format(nowDate)
                                + "] no trip today");
                    }
                    return null;
                }
                String tripEndTime = foundTrip.get(eIdx);
                if (tripEndTime.isEmpty()) {
                    return null;
                }
                Date eDate = hhMM.parse(tripEndTime);
                // add day if end date after 00:00
                eDate = eDate.before(startTrip) ? new Date(eDate.getTime()
                        + TimeUnit.DAYS.toMillis(1)) : eDate;
                data.add(new BusStopTime[] {
                            new BusStopTime(sBs, sDate),
                            new BusStopTime(eBs, eDate)
                        });
                cDate = eDate;
                if (i == 0) {
                    os.setStartDate(sDate);
                }
                i++;
            }
            os.setData(data);
            os.setArrivalDate(cDate);
            long dtime = data.get(0)[0].getTime().getTime()
                    - data.get(data.size() - 1)[1].getTime().getTime();
            os.setDuration(new Date(epoch - dtime));
            return os;
        }
    }
}
