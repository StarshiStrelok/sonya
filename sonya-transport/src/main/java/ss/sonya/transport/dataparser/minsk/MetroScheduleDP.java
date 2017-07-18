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
package ss.sonya.transport.dataparser.minsk;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ss.sonya.entity.BusStop;
import ss.sonya.entity.Path;
import ss.sonya.entity.Route;
import ss.sonya.entity.Trip;
import ss.sonya.inject.DataService;

/**
 * Metro schedule data parser.
 * @author ss
 */
@Component()
class MetroScheduleDP {
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(MetroScheduleDP.class);
    /** Data service. */
    @Autowired
    private DataService dataService;
    /**
     * Extract metro schedule.
     * @param routes metro routes.
     * @return metro schedule.
     */
    public Map<Path, List<Trip>> extract(final List<Route> routes) {
        try {
            return parse(getClass().getResourceAsStream(
                    "/ss/kira/data/minsk/metro.schedule.properties"), routes);
        } catch (Exception e) {
            LOG.error("extract metro schedule data fail!", e);
            return Collections.emptyMap();
        }
    }
    private Map<Path, List<Trip>> parse(final InputStream data,
            final List<Route> routes) throws Exception {
        Map<Integer, Path> pathMap = new HashMap<>();
        for (Route mRoute : routes) {
            for (Path mPath : mRoute.getPaths()) {
                pathMap.put(mPath.getExternalId().intValue(), mPath);
            }
        }
        Properties props = new Properties();
        props.load(data);
        Map<Integer, Map<String, String>> map = new HashMap<>();
        for (Object oKey : props.keySet()) {
            LOG.debug("key --> " + oKey);
            String[] split = oKey.toString().split("\\.");
            Integer id = Integer.valueOf(split[1]);
            String val = props.getProperty(oKey.toString());
            String k;
            if (split.length == 3) {
                k = split[2];
            } else {
                k = split[2] + "." + split[3];
            }
            if (map.containsKey(id)) {
                map.get(id).put(k, val);
            } else {
                Map<String, String> m = new HashMap<>();
                m.put(k, val);
                map.put(id, m);
            }
        }
        Map<Path, List<Trip>> tripsMap = new HashMap<>();
        for (Integer id : map.keySet()) {
            Path path = pathMap.get(id);
            LOG.debug("start path ==> " + path);
            List<Trip> schedule = new ArrayList<>();
            Map<String, String> m = map.get(id);
            Integer bs = Integer.valueOf(m.get("bs"));
            Integer duration = Integer.valueOf(m.get("duration"));
            if (m.containsKey("w")) {
                schedule.add(createTrips("2,3,4,5,6", m.get("w"),
                        m.get("w.interval"), bs, duration));
            }
            if (m.containsKey("e")) {
                schedule.add(createTrips("1,2,3,4,5,6,7", m.get("e"),
                        m.get("e.interval"), bs, duration));
            }
            if (m.containsKey("h")) {
                schedule.add(createTrips("1,7", m.get("h"),
                        m.get("h.interval"), bs, duration));
            }
            if (m.containsKey("1")) {
                schedule.add(createTrips("1", m.get("1"),
                        m.get("1.interval"), bs, duration));
            }
            if (m.containsKey("7")) {
                schedule.add(createTrips("7", m.get("7"),
                        m.get("7.interval"), bs, duration));
            }
            if (m.containsKey("234567")) {
                schedule.add(createTrips("2,3,4,5,6,7", m.get("234567"),
                        m.get("234567.interval"), bs, duration));
            }
            if (m.containsKey("2345")) {
                schedule.add(createTrips("2,3,4,5", m.get("2345"),
                        m.get("2345.interval"), bs, duration));
            }
            if (m.containsKey("6")) {
                schedule.add(createTrips("6", m.get("6"),
                        m.get("6.interval"), bs, duration));
            }
            if (schedule.isEmpty()) {
                throw new IllegalArgumentException("wrong schedule for "
                        + path);
            }
            LOG.debug(path + " has " + schedule.size() + " trips");
            tripsMap.put(path, schedule);
        }
        return tripsMap;
    }
// ============================= PRIVATE ======================================
    /**
     * Create trips.
     * @param days - days.
     * @param line - start time separate comma.
     * @param interval - interval.
     * @param bsId - bus stop ID.
     * @return - trip.
     * @throws Exception - method error.
     */
    private Trip createTrips(final String days, final String line,
            final String interval, final Integer bsId, final Integer duration)
            throws Exception {
        LOG.debug("days [" + days + "]");
        LOG.debug("line [" + line + "]");
        LOG.debug("interval [" + interval + "]");
        BusStop bs = dataService.findById(bsId, BusStop.class);
        LOG.debug("bs [" + bs.getName() + "]");
        LOG.debug("duration [" + duration  +"]");
        Trip trip = new Trip();
        trip.setDays(days);
        trip.setRegular("");
        JSONArray iArr = new JSONArray();
        for (String intStr : interval.split(";")) {
            iArr.put(intStr.replace(".", ":"));
        }
        StringTokenizer st = new StringTokenizer(line, " ");
        JSONArray ta = new JSONArray();
        SimpleDateFormat sdf_hh_mm = new SimpleDateFormat("HH:mm");
        SimpleDateFormat sdf_hh_mm_2 = new SimpleDateFormat("HH.mm");
        while (st.hasMoreTokens()) {
            ta.put(sdf_hh_mm.format(
                    sdf_hh_mm_2.parse(st.nextToken())));
        }
        JSONObject o = new JSONObject();
        o.put("interval", iArr);
        o.put("time", ta);
        o.put("duration", duration == null ? "" : (duration + ""));
        trip.setIrregular(o.toString());
        LOG.debug("additional --> " + trip.getIrregular());
        return trip;
    }
}
