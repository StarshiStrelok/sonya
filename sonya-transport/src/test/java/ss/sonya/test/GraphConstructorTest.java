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
package ss.sonya.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ss.sonya.entity.BusStop;
import ss.sonya.inject.service.Geometry;
import ss.sonya.transport.api.TransportDataService;
import ss.sonya.transport.search.SearchEngine;
import ss.sonya.transport.search.vo.OptimalPath;
import ss.sonya.transport.search.vo.SearchSettings;

/**
 *
 * @author ss
 */
public class GraphConstructorTest extends TestConfig {
    @BeforeClass
    public static void initTest() {
        // !!! PRODUCTION DATABASE, ONLY FOR READ
        System.setProperty("catalina.base",
                "/home/ss/kira/apache-tomcat-9.0.0.M19");
    }
    @Autowired
    private SearchEngine searchEngine;
    @Autowired
    private TransportDataService transportService;
    @Autowired
    private Geometry geometry;
    @Test
    //@Ignore
    public void test() throws Exception {
        SearchSettings s = new SearchSettings();
        s.setStartLat(53.919455733816385);
        s.setStartLon(27.434507391390493);
        s.setEndLat(53.9641268043094);
        s.setEndLon(27.556245406084717);
        s.setProfileId(4);
        s.setMaxTransfers(3);
        s.setMaxResults(5);
        s.setTime("21:12");
        s.setDay(3);
        s.setDisabledRouteTypes(Collections.emptyList());
        for (OptimalPath op : searchEngine.search(s)) {
            System.out.println(op.toString());
        }
    }
    @Test
    @Ignore
    public void test2() throws Exception {
        final int profileId = 4;
        double swLon = Double.MAX_VALUE;
        double swLat = Double.MAX_VALUE;
        double neLon = Double.MIN_VALUE;
        double neLat = Double.MIN_VALUE;
        List<BusStop> all = transportService.getFromProfile(profileId, BusStop.class);
        for (BusStop bs : all) {
            if (swLon > bs.getLongitude()) {
                swLon = bs.getLongitude();
            }
            if (swLat > bs.getLatitude()) {
                swLat = bs.getLatitude();
            }
            if (neLon < bs.getLongitude()) {
                neLon = bs.getLongitude();
            }
            if (neLat < bs.getLatitude()) {
                neLat = bs.getLatitude();
            }
        }
        System.out.println("SW_LAT: " + swLat);
        System.out.println("SW_LON: " + swLon);
        System.out.println("NE_LAT: " + neLat);
        System.out.println("NE_LON: " + neLon);
        int attempt = 1000;
        long start = System.currentTimeMillis();
        List<OptimalPath> list;
        int empty = 0;
        long s;
        long delta;
        List<Integer[]> data = new ArrayList<>();
        long max = Long.MIN_VALUE;
        for (int i = 0; i < attempt; i++) {
            s = System.currentTimeMillis();
            double eLat = swLat + (neLat - swLat) * new Random().nextDouble();
            double eLon = swLon + (neLon - swLon) * new Random().nextDouble();
            double sLat = swLat + (neLat - swLat) * new Random().nextDouble();
            double sLon = swLon + (neLon - swLon) * new Random().nextDouble();
            if (geometry.calcDistance(sLat, sLon, eLat, eLon) < 1) {
                i--;
                continue;
            }
            SearchSettings settings = new SearchSettings();
            settings.setStartLat(sLat);
            settings.setStartLon(sLon);
            settings.setEndLat(eLat);
            settings.setEndLon(eLon);
            settings.setDay(5);
            settings.setProfileId(profileId);
            settings.setTime("11:41");
            settings.setMaxResults(10);
            settings.setMaxTransfers(4);
            settings.setDisabledRouteTypes(Collections.emptyList());
            list = searchEngine.search(settings);
            if (list.isEmpty()) {
                empty++;
            }
            delta = System.currentTimeMillis() - s;
            data.add(new Integer[] {(int) delta, list.size()});
            if (delta > max) {
                max = delta;
            }
        }
        Map<Integer, Integer> m = new HashMap<>();
        for (Integer[] p : data) {
            long percent = p[0] * 100 / max;
            if (m.containsKey((int) percent)) {
                m.put((int) percent, m.get((int) percent) + 1);
            } else {
                m.put((int) percent, 1);
            }
        }
        List<Integer> ks = new ArrayList<>(m.keySet());
        Collections.sort(ks);
        for (Integer k : ks) {
            System.out.println(String.format("%-7s", m.get(k)) + String.format("%-11s", " [~"
                    + (k * max / 100) + "] ms") + " : "
                    + new String(new char[k]).replace("\0", "."));
        }
        System.out.println("max time [" + max + "] ms");
        System.out.println("avg time [" + (System.currentTimeMillis() - start) / attempt + "]");
        System.out.println("empty [" + empty  + "]");
    }
}
