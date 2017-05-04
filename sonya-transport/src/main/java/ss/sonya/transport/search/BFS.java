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
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import ss.sonya.entity.BusStop;
import ss.sonya.entity.TransportProfile;
import ss.sonya.inject.service.Geometry;
import ss.sonya.transport.component.TransportGeometry;
import ss.sonya.transport.search.vo.OptimalPath;

/**
 * BFS abstract class.
 * @author ss
 */
public abstract class BFS implements SearchEngine {
    /** Logger. */
    protected static final Logger LOG = Logger.getLogger(BFS.class);
    /** Transport geometry. */
    @Autowired
    protected TransportGeometry transportGeometry;
    /** Geometry. */
    @Autowired
    protected Geometry geometry;
    /** Graph constructor. */
    @Autowired
    protected GraphConstructor graphConstructor;
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
    protected abstract Map<Integer, Set<BusStop>> createPointVertices(
            List<BusStop> pointBusStops, boolean isStart,
            TransportProfile profile, Graph graph) throws Exception;
    /**
     * Find straight paths.
     * @param startVertices start vertices.
     * @param endVertices end vertices.
     * @param graph graph.
     * @return straight paths list.
     * @throws Exception method error.
     */
    protected abstract List<OptimalPath> straightPaths(
            Map<Integer, Set<BusStop>> startVertices,
            Map<Integer, Set<BusStop>> endVertices,
            Graph graph) throws Exception;
    /**
     * Sort result.
     * @param result result.
     * @throws Exception error.
     */
    protected abstract void sortResults(List<OptimalPath> result)
            throws Exception;
    /**
     * Clear unreal results.
     * @param dirty dirty optimal paths.
     * @param startBs all start bus stops.
     * @return real optimal paths.
     * @throws Exception method error.
     */
    protected List<OptimalPath> clearUnrealResults(
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
}
