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

import java.util.Date;

/**
 * Bus stop + it time.
 * @author ss
 */
public class BusStopTime {
    /** Bus stop name. */
    private String busstop;
    /** Bus stop time. */
    private Date time;
    /**
     * @return the busstop
     */
    public String getBusstop() {
        return busstop;
    }
    /**
     * @param busstop the busstop to set
     */
    public void setBusstop(String busstop) {
        this.busstop = busstop;
    }
    /**
     * @return the time
     */
    public Date getTime() {
        return time;
    }
    /**
     * @param time the time to set
     */
    public void setTime(Date time) {
        this.time = time;
    }
}
