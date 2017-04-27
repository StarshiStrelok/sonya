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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ss.sonya.entity.BusStop;
import ss.sonya.entity.RouteProfile;
import ss.sonya.entity.Trip;
import ss.sonya.inject.service.Geometry;

/**
 * Build schedule using movement interval.
 * @author ss
 */
@Component
public class VirtualScheduleBuilder {
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(
            VirtualScheduleBuilder.class);
    /** Bus stop cost, min. */
    private static final double BUS_STOP_COST = 0.5;
    /** Geometry component. */
    @Autowired
    private Geometry geometry;
    /** Transport geometry. */
    @Autowired
    private TransportGeometry transportGeometry;
    /**
     * Build virtual schedule (irregular schedule).
     * @param schedule path schedule with interval info only.
     * @param fullWay path full way.
     * @param type route type.
     * @return trips.
     * @throws Exception - operation error.
     */
    public List<Trip> buildVirtualSchedule(final List<Trip> schedule,
            final List<BusStop> fullWay,
            final RouteProfile type) throws Exception {
        SimpleDateFormat hhMM = new SimpleDateFormat("HH:mm");
        List<Trip> trips = new ArrayList<>();
        if (schedule.isEmpty()) {
            return trips;
        }
        Calendar c = GregorianCalendar.getInstance();
        for (Trip t : schedule) {
            if (t.getIrregular() == null
                    || t.getIrregular().isEmpty()) {
                continue;
            }
            JSONObject o = new JSONObject(t.getIrregular());
            // prepare start dates from concrete time
            List<Date> startDates = new ArrayList<>();
            JSONArray times = o.getJSONArray("time");
            for (int i = 0; i < times.length(); i++) {
                startDates.add(hhMM.parse(times.getString(i)));
            }
            // prepare start dates from intervals
            JSONArray intervals = o.getJSONArray("interval");
            for (int i = 0; i < intervals.length(); i++) {
                String str = intervals.getString(i);
                String timeStr = str.substring(0, str.indexOf("="));
                String intStr = str.substring(str.indexOf("=") + 1);
                Date sDate = hhMM.parse(timeStr.substring(0,
                        timeStr.indexOf("-")));
                Date eDate = hhMM.parse(timeStr.substring(
                        timeStr.indexOf("-") + 1));
                int sInt = Integer.valueOf(intStr.substring(0,
                            intStr.indexOf("-")));
                int eInt = Integer.valueOf(intStr.substring(
                        intStr.indexOf("-") + 1));
                int intervalMin = (sInt + eInt) / 2;        // min
                c.setTime(sDate);
                while (sDate.before(eDate)) {
                    if (!startDates.contains(sDate)) {
                        startDates.add(sDate);
                    }
                    // increase date by an amount the interval
                    c.add(Calendar.MINUTE, intervalMin);
                    sDate = c.getTime();
                }
            }
            double distance = transportGeometry.calcWayDistance(fullWay); // km
            double time;
            if (o.has("duration")) {
                time = o.getDouble("duration");
            } else {
                time = distance / type.getAvgSpeed()
                    * TimeUnit.HOURS.toMinutes(1)
                    + (BUS_STOP_COST * fullWay.size());       // min
            }
            for (Date date : startDates) {
                c.setTime(date);
                Trip trip = new Trip();
                trip.setDays(t.getDays());
                StringBuilder tripInfo = new StringBuilder();
                tripInfo.append(hhMM.format(date)).append(",");
                Iterator<BusStop> itr = fullWay.iterator();
                BusStop prev = null;
                while (itr.hasNext()) {
                    BusStop bs = itr.next();
                    if (prev == null) {
                        prev = bs;
                        continue;
                    }
                    double subDist = geometry.calcDistance(prev.getLatitude(),
                            prev.getLongitude(), bs.getLatitude(),
                            bs.getLongitude());                     // km
                    double subTime = subDist / distance * time
                            * TimeUnit.MINUTES.toSeconds(1);     // sec
                    c.add(Calendar.SECOND, (int) subTime);
                    String bsTime = hhMM.format(c.getTime());
                    if (LOG.isTraceEnabled()) {
                        LOG.trace(bs.getName() + " --> " + bsTime);
                    }
                    tripInfo.append(bsTime).append(",");
                    prev = bs;
                }
                tripInfo.setLength(tripInfo.length() - 1);
                trip.setRegular(tripInfo.toString());
                if (LOG.isTraceEnabled()) {
                    LOG.trace("trip --> " + trip.getRegular());
                }
                trips.add(trip);
            }
        }
        return trips;
    }
}
