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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import ss.sonya.transport.constants.TransportConst;
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
        Set<BusStop> all = new HashSet<>();
        graph.getAllPaths().forEach(p -> {
            if (!settings.getDisabledRouteTypes()
                    .contains(p.getRoute().getType())) {
                all.addAll(p.getBusstops());
            }
        });
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
        LOG.info("#-bfs-# max transfers [" + settings.getMaxTransfers() + "]");
        if (settings.getMaxTransfers() > 0) {
            // reverse search for performance
            boolean isReverseSearch = startVertices.size() > endVertices.size();
            LOG.info("#-bfs-# reverse search [" + isReverseSearch + "]");
            Map<Integer, Set<BusStop>> pseudoStartVertices = isReverseSearch
                    ? endVertices : startVertices;
            Map<Integer, Set<BusStop>> pseudoEndVertices = isReverseSearch
                    ? startVertices : endVertices;
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
            Iterator<Integer> itr = pseudoStartVertices.keySet().iterator();
            int counter = 0;
            while (itr.hasNext()) {
                portions[counter % cores].add(itr.next());
                counter++;
            }
            for (List<Integer> portion : portions) {
                futures.add(ex.submit(
                        new BFSTask(portion, pseudoEndVertices,
                                pseudoStartVertices, graph,
                                settings.getMaxTransfers(), isReverseSearch)
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
        if (!settings.getDisabledRouteTypes().isEmpty()) {
            result = excludeDisabledRoutes(result, settings);
        }
        result = groupingResult(result, settings);
        result = filterDuplicates(result);
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
        List<Integer> excludes = new ArrayList<>();
        for (Integer s : startVertices.keySet()) {
            for (Integer e : endVertices.keySet()) {
                // vertex exist in start & end points - straight path
                if (s.intValue() == e.intValue()) {
                    excludes.add(e);
                    excludes.add(s);
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
        // exclude vertices which belong to both criteria (straight vert.)
        for (Integer v : excludes) {
            endVertices.remove(v);
            startVertices.remove(v);
            if (LOG.isDebugEnabled()) {
                LOG.debug("#-bfs-# exclude vertex from criteries ["
                        + v + "], it straight vertex");
            }
            startVertices.remove(v);
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
            String time;
            int day;
            if (settings.isCurrentTimeAndDate()) {
                Date now = new Date();
                Calendar c = new GregorianCalendar();
                c.setTime(now);
                time = new SimpleDateFormat("HH:mm").format(now);
                day = c.get(Calendar.DAY_OF_WEEK);
            } else {
                time = settings.getTime();
                day = settings.getDay();
            }
            insertSchedule(result, time, day, graph);
            Collections.sort(result, (OptimalPath o1, OptimalPath o2) -> {
                if (o1.getTransfers() > o2.getTransfers()) {
                    return 1;
                } else if (o1.getTransfers() < o2.getTransfers()) {
                    return -1;
                } else {
                    Date d1 = o1.getSchedule().getArrivalDate();
                    Date d2 = o2.getSchedule().getArrivalDate();
                    if (d1.getTime() < d2.getTime()) {
                        return -1;
                    } else if (d1.getTime() > d2.getTime()) {
                        return 1;
                    } else {
                        if (o1.getTime() > o2.getTime()) {
                            return 1;
                        } else if (o1.getTime() < o2.getTime()) {
                            return -1;
                        } else {
                            return 0;
                        }
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
    private List<OptimalPath> groupingResult(final List<OptimalPath> dirty,
            final SearchSettings settings) throws Exception {
        long start = System.currentTimeMillis();
        double sLat = settings.getStartLat();
        double sLon = settings.getStartLon();
        double eLat = settings.getEndLat();
        double eLon = settings.getEndLon();
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
        for (List<OptimalPath> list : grouping.values()) {
            OptimalPath best = selectBest(list, sLat, sLon, eLat, eLon);
            total.add(best);
        }
        LOG.info("#-bfs-# total paths size [" + total.size() + "]");
        LOG.info("#-bfs-# grouping elapsed time ["
                + (System.currentTimeMillis() - start) + "]");
        return total;
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
            } catch (Exception ex1) {
                LOG.error("insert schedule task error!", ex1);
            }
        }
        List<OptimalPath> withSchedule = new ArrayList<>();
        opList.stream().forEach(op -> {
            if (op.getSchedule() != null) {
                withSchedule.add(op);
            }
        });
        opList.clear();
        opList.addAll(withSchedule);
        LOG.info("#-bfs-# insert schedule elapsed time ["
                + (System.currentTimeMillis() - start) + "] ms");
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
         * @param pTime start trip time.
         * @param pDay trip day.
         * @param g graph.
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
                    double humanDist = geometry.calcDistance(
                            prevBs.getLatitude(),
                            prevBs.getLongitude(), sBs.getLatitude(),
                            sBs.getLongitude());        // km
                    double addTime = humanDist
                            / TransportConst.HUMAN_SPEED;
                    double addTimeSec = addTime * TimeUnit.HOURS.toMinutes(1)
                            * TimeUnit.MINUTES.toSeconds(1);      // sec
                    nowDate = new Date(nowDate.getTime()
                            + (long) (addTimeSec
                                    * TimeUnit.SECONDS.toMillis(1)));
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
    /**
     * Filter duplicate among optimal paths.
     * @param result all optimal paths.
     * @return filtered list of optimal paths.
     */
    private List<OptimalPath> filterDuplicates(final List<OptimalPath> result) {
        long start = System.currentTimeMillis();
        List<OptimalPath> filtered = new ArrayList<>();
        Map<Integer, List<OptimalPath>> map = new HashMap<>();
        for (OptimalPath op : result) {
            Integer key = op.getPath().size();
            if (map.containsKey(key)) {
                map.get(key).add(op);
            } else {
                List<OptimalPath> list = new ArrayList<>();
                list.add(op);
                map.put(key, list);
            }
        }
        Set<String> relevance = new HashSet<>();
        List<Integer> sortedLevels = new ArrayList<>(map.keySet());
        Collections.sort(sortedLevels);
        for (Integer level : sortedLevels) {
            for (OptimalPath op : map.get(level)) {
                if (level == 1) {
                    filtered.add(op);
                    relevance.add(op.getPath().get(0).getId() + "");
                } else {
                    StringBuilder opKeySb = new StringBuilder();
                    for (Path p : op.getPath()) {
                        opKeySb.append(p.getId()).append("#");
                    }
                    String opKey = opKeySb.toString();
                    boolean isMatch = false;
                    for (String relKey : relevance) {
                        if (opKey.contains(relKey)) {
                            isMatch = true;
                            break;
                        }
                    }
                    if (!isMatch) {
                        filtered.add(op);
                        relevance.add(opKey);
                    }
                }
            }
        }
        LOG.info("#-bfs-# filter duplicates: was [" + result.size()
                + "], rest [" + filtered.size() + "], elapsed time ["
                + (System.currentTimeMillis() - start) + "]");
        return filtered;
    }
    /**
     * Exclude disabled routes (if user not want see any route in result).
     * @param result all optimal paths.
     * @param settings search settings.
     * @return filtered list.
     */
    private List<OptimalPath> excludeDisabledRoutes(
            final List<OptimalPath> result, final SearchSettings settings) {
        long start = System.currentTimeMillis();
        List<OptimalPath> filtered = new ArrayList<>();
        for (OptimalPath op : result) {
            boolean isMatch = false;
            for (Path p : op.getPath()) {
                if (settings.getDisabledRouteTypes()
                        .contains(p.getRoute().getType())) {
                    isMatch = true;
                    break;
                }
            }
            if (!isMatch) {
                filtered.add(op);
            }
        }
        LOG.info("#-bfs-# exclude disabled routes: was [" + result.size()
                + "], rest [" + filtered.size() + "], elapsed time ["
                + (System.currentTimeMillis() - start) + "]");
        return filtered;
    }
}
