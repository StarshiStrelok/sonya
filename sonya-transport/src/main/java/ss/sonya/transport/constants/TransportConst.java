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
package ss.sonya.transport.constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

/**
 * Transport constants.
 * @author ss
 */
public final class TransportConst {
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(TransportConst.class);
    /** Mock bus stop. */
    public static final String MOCK_BS = "mock";
    /** Specific route profile name, means subway. */
    public static final String METRO = "Metro";
    /** Transport midnight, for example 3:00. */
    public static final long TRANSPORT_MIDNIGHT = TimeUnit.HOURS.toMillis(3);
    /** Average human speed, km/h. */
    public static final double HUMAN_SPEED = 4;
    /** Transfer time payment. In hours. */
    public static final double TRANSFER_TIME_PAYMENT = 10 / 60;
    /** Contains times from 00:00 - 23:59. */
    public static final Map<String, Long> ALL_TIMES = new HashMap<>();
    /** Initialization. */
    static {
        Calendar c = GregorianCalendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        try {
            c.setTime(sdf.parse("00:00"));
        } catch (ParseException ex) {
            LOG.fatal("wrong zero time will be setted!", ex);
        }
        long minInDay = TimeUnit.DAYS.toMinutes(1);
        for (int i = 0; i < minInDay; i++) {
            ALL_TIMES.put(sdf.format(c.getTime()), c.getTime().getTime());
            c.add(Calendar.MINUTE, 1);
        }
    }
    /**
     * Private constructor.
     */
    private TransportConst() {
    }
}
