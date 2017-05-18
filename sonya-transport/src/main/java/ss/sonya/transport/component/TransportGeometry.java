/*
 * Copyright (C) 2016 ss
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
package ss.sonya.transport.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ss.sonya.constants.TransportConst;
import ss.sonya.entity.BusStop;
import ss.sonya.entity.Route;
import ss.sonya.inject.service.Geometry;
import ss.sonya.transport.search.vo.OptimalPath;

/**
 * Transport geometry.
 * @author ss
 */
@Component
public class TransportGeometry {
    /** Geometry. */
    @Autowired
    private Geometry geometry;
    /**
     * Find fixed number closest bus stops.
     * @param limit maximum result size.
     * @param all all bus stops.
     * @param lat latitude.
     * @param lon longitude.
     * @return list bus stops.
     */
    public List<BusStop> findNearestBusStops(final int limit,
        final Set<BusStop> all, final double lat, final double lon) {
        double dist;
        Map<Double, BusStop> map = new HashMap<>();
        for (BusStop b : all) {
            if (TransportConst.MOCK_BS.equals(b.getName())) {
                continue;
            }
            dist = geometry.calcDistance(b.getLatitude(), b.getLongitude(),
                    lat, lon);
            map.put(dist, b);
        }
        List<BusStop> result = new ArrayList<>();
        List<Double> keys = new ArrayList<>(map.keySet());
        Collections.sort(keys);
        if (keys.size() < limit) {
            return new ArrayList<>(map.values());
        }
        for (int i = 0; i < limit; i++) {
            result.add(map.get(keys.get(i)));
        }
        return result;
    }
    /**
     * Find bus stops in radius.
     * @param all - all bus stops.
     * @param bs - target bus stop.
     * @param radius - radius in km.
     * @return - bus stops near target bus stop. If result == 0, return closest
     * bus stop outside radius.
     */
    public List<BusStop> findBusStopsInRadius(final List<BusStop> all,
            final BusStop bs, final double radius) {
        return findBusStopsInRadius(all, bs.getLatitude(), bs.getLongitude(),
                radius);
    }
    /**
     * Find bus stops in radius.
     * @param all - all bus stops.
     * @param lat - center latitude.
     * @param lon - center longitude.
     * @param radius - radius in km.
     * @return - bus stops near target bus stop. If result == 0, return closest
     * bus stop outside radius.
     */
    public List<BusStop> findBusStopsInRadius(final List<BusStop> all,
            final double lat, final double lon, final double radius) {
        List<BusStop> result = new ArrayList<>();
        double dist;
        for (BusStop b : all) {
            if (TransportConst.MOCK_BS.equals(b.getName())) {
                continue;
            }
            dist = geometry.calcDistance(b.getLatitude(), b.getLongitude(),
                    lat, lon);
            if (dist <= radius) {
                result.add(b);
            }
        }
        return result;
    }
    /**
     * Find closest to center bus stop from way.
     * @param way - way.
     * @param center - center bus stop.
     * @return - closest bus stop.
     */
    public BusStop findClosestBusStop(final List<BusStop> way,
            final BusStop center) {
        return findClosestBusStop(way, center.getLatitude(),
                center.getLongitude());
    }
    /**
     * Find closest to center bus stop from way.
     * @param way - way.
     * @param lat - center latitude.
     * @param lon - center longitude.
     * @return - closest bus stop.
     */
    public BusStop findClosestBusStop(final List<BusStop> way,
            final double lat, final double lon) {
        double dist;
        BusStop closest = null;
        double bestDist = Double.MAX_VALUE;
        for (BusStop b : way) {
            if (TransportConst.MOCK_BS.equals(b.getName())) {
                continue;
            }
            dist = geometry.calcDistance(b.getLatitude(), b.getLongitude(),
                    lat, lon);
            if (dist < bestDist) {
                closest = b;
                bestDist = dist;
            }
        }
        return closest;
    }
    /**
     * Calculate way distance approximately.
     * @param way - way.
     * @return - way distance, km.
     */
    public double calcWayDistance(final List<BusStop> way) {
        double sum = 0d;
        Iterator<BusStop> itr = way.iterator();
        BusStop prev = null;
        while (itr.hasNext()) {
            if (prev == null) {
                prev = itr.next();
                continue;
            }
            BusStop cur = itr.next();
            if (TransportConst.MOCK_BS.equals(cur.getName())) {
                continue;
            }
            double dist = geometry.calcDistance(prev.getLatitude(),
                    prev.getLongitude(), cur.getLatitude(), cur.getLongitude());
            sum += dist;
            prev = cur;
        }
        return sum;
    }
    /**
     * Calculate optimal path time and distance.
     * @param op - optimal path.
     * @throws Exception - operation error.
     */
    public void calcOptimalPathTime(final OptimalPath op) throws Exception {
        double transferDist = 0;
        double transportTime = 0;
        double dist = 0;
        List<BusStop> prevSubWay = null;
        Route r;
        int cur = 0;
        double speed;
        for (List<BusStop> subWay : op.getWay()) {
            if (prevSubWay != null) {
                BusStop prevBs = prevSubWay.get(prevSubWay.size() - 1);
                BusStop curBs = subWay.get(0);
                transferDist += geometry.calcDistance(prevBs.getLatitude(),
                        prevBs.getLongitude(), curBs.getLatitude(),
                        curBs.getLongitude());
            }
            r = op.getPath().get(cur).getRoute();
            speed = r.getType().getAvgSpeed();
            double subDist = calcWayDistance(subWay);
            transportTime += subDist / speed;
            dist += subDist;
            prevSubWay = subWay;
            cur++;
        }
        double totalTime = transferDist / TransportConst.HUMAN_SPEED
                + transportTime
                + TransportConst.TRANSFER_TIME_PAYMENT
                    * (op.getPath().size() - 1);
        op.setTime(totalTime);
        op.setDistance(dist);
    }
}
