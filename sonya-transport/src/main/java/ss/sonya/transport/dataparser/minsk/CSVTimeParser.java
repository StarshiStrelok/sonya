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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import ss.sonya.transport.constants.TransportConst;
import ss.sonya.entity.Path;
import ss.sonya.entity.Route;
import ss.sonya.entity.Trip;

/**
 * CSV parser.
 * @author ss
 */
@Component
class CSVTimeParser {
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(CSVTimeParser.class);
    /** Time format. */
    private static final SimpleDateFormat SDF_TIME = new SimpleDateFormat(
            "HH:mm");
    /** CSV resource. */
    private static final String FILE = "/ss/kira/data/minsk/times.csv";
    /** Errors count. */
    protected int errors = 0;
    /**
     * Extract autobus, trolleybus, tram schedule data.
     * @param routes autobus, trolleybus, tram routes.
     * @return schedule.
     */
    public Map<Path, List<Trip>> extract(final List<Route> routes) {
        errors = 0;
        try {
            Map<String, Map<Integer, List<List<String>>>> map = parse();
            LOG.debug("csv size [" + map.size() + "]");
            LOG.info("errors [" + errors + "]");
            return createTrips(routes, map);
        } catch (Exception e) {
            LOG.error("extract schedule data - fail!", e);
        }
        return Collections.emptyMap();
    }
    /**
     * Parse one line.
     * @param decoded_data - one line string.
     * @return - trips map.
     * @throws Exception - method error.
     */
    private Map<Integer, List<List<String>>> decode(final String decoded_data)
            throws Exception {
	List<Integer> timetable = new ArrayList<>();
	List<Integer> weekdays = new ArrayList<>();
	List<Object> valid_from = new ArrayList<>();
	List<Object> valid_to = new ArrayList<>();
	int w;
	int h;
	String[] times = decoded_data.split(",");
	int i, prev_t;
	int i_max = times.length;
	List<Object> zero_ground=new ArrayList<>();
        for (int u = 0; u < 300; u++) {
            zero_ground.add(null);
        }
        char plus = '+';
        char minus = '-';
	for (i = -1, w = 0, h=0, prev_t = 0; ++i < i_max;) {
		String t = times[i];
		if (t.isEmpty()) {
			break;
		}
		char tag = t.charAt(0);
		if (tag == plus || (tag == minus && t.charAt(1)=='0')) {
		    zero_ground.add(i, "1");
		}
		prev_t += +(Integer.valueOf(t));
		timetable.add(w++, prev_t);
	}   
	for (int j = zero_ground.size(); --j >= 0;) {
	    if (zero_ground.get(j) != null) {
	        zero_ground.add(j,"0");
	    }    
	}
	for (int j = 0; ++i < i_max;) {
		int day = +(Integer.valueOf(times[i]));
		String k = i + 1 >= times.length ? "" : times[++i];
		if (k.isEmpty()) {
			k = (w - j) + "";
			i_max = 0;
		}
		else {
		  k = +("".equals(k) ? 0 : Integer.parseInt(k)) + "";
		}
		
		while (Integer.valueOf(k) > 0) {
		  valid_from.add(j++, day);
                  k = (Integer.valueOf(k) - 1) + "";
		}
	}
	--i;
	i_max = times.length;
	for (int j = 0; ++i < i_max;) {
		int day = +(Integer.valueOf(times[i]));
		String k = times[++i];
		if (k.isEmpty()) {
			k = (w - j) + "";
			i_max = 0;
		}
		else {
		  k = +(Integer.valueOf(k)) + "";
		}
		while (Integer.valueOf(k) > 0) {
		  valid_to.add(j++,day);
                  k = (Integer.valueOf(k) - 1) + "";
		}
	}
	--i;
	i_max = times.length;
	for (int j = 0; ++i < i_max;) {
		int weekday = Integer.valueOf(times[i]);
		String k = times[++i];
		if (k.isEmpty()) {
			k = (w - j) + "";
			i_max = 0;
		}
		else {
		  k = +(Integer.valueOf(k)) + "";
		}
		while (Integer.valueOf(k) > 0) {
		  weekdays.add(j++, weekday);
                  k = (Integer.valueOf(k) - 1) + "";
		}
	}
	--i;
	h = 1;
	i_max = times.length;
	for (int j = w, w_left = w, dt = 5; ++i < i_max;) {
		dt += +(Integer.valueOf(times[i])) -5;
		String k = i + 1 >= times.length ? "" : times[++i];
		if (!k.isEmpty()) {
			k = +(Integer.valueOf(k)) + "";
			w_left -= Integer.valueOf(k);
		}
		else {
			k = w_left + "";
			w_left = 0;
		}
		
		while (Integer.valueOf(k) > 0) {
		  timetable.add(j, dt + timetable.get(j-w));
		  ++j;  
                  k = (Integer.valueOf(k) - 1) + "";
		}
	
		if (w_left <= 0) {
			w_left = w;
			dt = 5;
			++h;
		}
	}
        Map<Integer, List<List<String>>> map = new HashMap<>();
        List<String> tList = new ArrayList<>();
        int height = timetable.size() / weekdays.size();
        int width = weekdays.size();
        LOG.debug("Height [" + height + "]");
        LOG.debug("Width [" + width + "]");
        int prevDay = weekdays.get(0);
        Map<Integer, List<String>> tmpMap = new HashMap<>();
        for (int j = 0; j < timetable.size(); j++) {
            tList.add(SDF_TIME.format(new Date(
                        TimeUnit.MINUTES.toMillis(
                                Long.valueOf(timetable.get(j)))
                                        - SDF_TIME.getTimeZone()
                                                .getRawOffset())));
            int pos = (j + 1) % width;
            int day = weekdays.get(pos);
            LOG.trace("pos [" + pos + "], day [" + day + "]");
            if (pos == 0
                    || (prevDay != -1 && prevDay != day)) {
                if (!tmpMap.containsKey(prevDay)) {
                    tmpMap.put(prevDay, new ArrayList<>());
                }
                tmpMap.get(prevDay).addAll(tList);
                tList = new ArrayList<>();
            }
            prevDay = day;
            if (pos == 0) {
                for (Integer days : tmpMap.keySet()) {
                    if (!map.containsKey(days)) {
                        map.put(days, new ArrayList<>());
                    }
                    map.get(days).add(tmpMap.get(days));
                }
                tmpMap = new HashMap<>();
            }
        }
        // output
        if (LOG.isTraceEnabled()) {
        for (Integer key : map.keySet()) {
            List<List<String>> matrix = map.get(key);
            LOG.debug("part size [" + matrix.size() + "], days ["
                    + key + "]");
            StringBuilder head = new StringBuilder();
            for (int j = 0; j < matrix.get(0).size(); j++) {
                head.append("+-----");
            }
            head.append("+");
            LOG.debug(head.toString());
            StringBuilder sb = new StringBuilder();
            for (List<String> row : matrix) {
                sb.setLength(0);
                for (String timeS : row) {
                    sb.append("|").append(timeS);
                }
                sb.append("|");
                LOG.debug(sb.toString());
            }
            LOG.debug(head.toString());
            LOG.debug("");
        }
        }
        // checked
        for (List<List<String>> matrix : map.values()) {
            int capacity = -1;
            for (List<String> row : matrix) {
                if (capacity == -1) {
                    capacity = row.size();
                }
                if (capacity != row.size()) {
                    errors++;
                    break;
                    //throw new IllegalArgumentException();
                }
            }
        }
        Map<Integer, List<List<String>>> result = new HashMap<>();
        for (Integer k : map.keySet()) {
            List<List<String>> matrix = map.get(k);
            List<List<String>> rMatrix = new ArrayList<>();
            for (int y = 0; y < matrix.get(0).size(); y++) {
                List<String> column = new ArrayList<>();
                for (int x = 0; x < matrix.size(); x++) {
                    column.add(matrix.get(x).get(y));
                }
                rMatrix.add(column);
            }
            Collections.sort(rMatrix, (List<String> o1, List<String> o2) -> {
                try {
                    Date t1 = SDF_TIME.parse(o1.get(0));
                    Date t2 = SDF_TIME.parse(o2.get(0));
                    long midnight = SDF_TIME.getTimeZone().getRawOffset()
                            - TransportConst.TRANSPORT_MIDNIGHT;
                    long l1 = t1.getTime() < midnight
                            ? t1.getTime() + TimeUnit.DAYS.toMillis(1)
                            : t1.getTime();
                    long l2 = t2.getTime() < midnight
                            ? t2.getTime() + TimeUnit.DAYS.toMillis(1)
                            : t2.getTime();
                    return l1 >= l2 ? 1 : -1;
                } catch (ParseException e) {
                    LOG.error("parse exception!", e);
                    return 0;
                }
            });
            result.put(k, rMatrix);
        }
        for (Integer k : result.keySet()) {
            List<List<String>> matrix = result.get(k);
            LOG.debug("trips [" + matrix.size() + "], days ["
                    + k + "]");
            StringBuilder head = new StringBuilder();
            for (int j = 0; j < matrix.size(); j++) {
                head.append("+-----");
            }
            head.append("+");
            LOG.debug(head.toString());
            StringBuilder sb = new StringBuilder();
            for (int r = 0; r < matrix.get(0).size(); r++) {
                sb.setLength(0);
                for (int c = 0; c < matrix.size(); c++) {
                    sb.append("|").append(matrix.get(c).get(r));
                }
                sb.append("|");
                LOG.debug(sb.toString());
            }
            LOG.debug(head.toString());
            LOG.debug("");
        }
        return result;
    }
    /**
     * Create trips.
     * @param routeType - route type.
     * @param map - data map.
     * @return - trips map.
     * @throws Exception - method error.
     */
    private Map<Path, List<Trip>> createTrips(final List<Route> routes,
            final Map<String, Map<Integer, List<List<String>>>> map)
            throws Exception{
        Map<Path, List<Trip>> result = new HashMap<>();
        for (Route route : routes) {
            LOG.debug("======= " + route);
            for (Path path : route.getPaths()) {
                int waySize = path.getBusstops().size();
                LOG.debug("=== " + path);
                Map<Integer, List<List<String>>> matrix =
                        map.get("" + path.getExternalId());
                if (matrix == null) {
                    throw new IllegalArgumentException("not found alt ID ["
                            + path.getExternalId() + "], " + path + ", " + route);
                }
                List<Trip> trips = new ArrayList<>();
                for (Integer days : matrix.keySet()) {
                    StringBuilder daysSb = new StringBuilder();
                    String str = days + "";
                    Set<Integer> set = new TreeSet<>();
                    for (int i = 0; i < str.length(); i++) {
                        int d = Integer.valueOf(str.charAt(i) + "") + 1;
                        d = d == 8 ? 1 : d;
                        set.add(d);
                    }
                    for (Integer i : set) {
                        daysSb.append(i).append(",");
                    }
                    daysSb.setLength(daysSb.length() - 1);
                    LOG.debug("days [" + daysSb + "]");
                    for (List<String> tripArr : matrix.get(days)) {
                        if (tripArr.size() != waySize) {
                            throw new IllegalArgumentException(
                                    "way no match, alt ID [" + path.getExternalId()
                                            + "], " + path + ", " + route);
                        }
                        Trip trip = new Trip();
                        trip.setDays(daysSb.toString());
                        StringBuilder timeSb = new StringBuilder();
                        for (String t : tripArr) {
                            timeSb.append(t).append(",");
                        }
                        timeSb.setLength(timeSb.length() - 1);
                        LOG.debug("time [" + timeSb + "]");
                        trip.setRegular(timeSb.toString());
                        trips.add(trip);
                    }
                }
                result.put(path, trips);
            }
            LOG.debug("\n\n");
        }
        return result;
    }
    /**
     * Parse CSV file.
     * @return - data.
     * @throws Exception - method error.
     */
    private Map<String, Map<Integer, List<List<String>>>> parse()
            throws Exception {
        LOG.info("######### start parsing...");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                    getClass().getResourceAsStream(FILE)))) {
            String line;
            String decoded;
            String altId;
            Map<String, Map<Integer, List<List<String>>>> map = new HashMap<>();
            while ((line = br.readLine()) != null) {
                if (!line.contains(",")) {
                    continue;
                }
                decoded = line.substring(line.indexOf(",") + 1);
                altId = line.substring(0, line.indexOf(","));
                LOG.debug("---------------------- " + altId + " -------------");
                map.put(altId, decode(decoded));
            }
            return map;
        } catch (IOException ex) {
            LOG.error("parse CSV error!", ex);
            return null;
        }
    }
}
