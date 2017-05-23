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
package ss.sonya.transport.search.vo;

import java.util.Date;
import java.util.List;

/**
 * Optimal schedule.
 * Contains the fastest schedule for it optimal path.
 * @author ss
 */
public class OptimalSchedule {
    /** Schedule data. */
    private List<BusStopTime[]> data;
    /** Arrival time. Date format. */
    private Date arrivalDate;
    /** Start date. */
    private Date startDate;
    /** Duration time. Date format. */
    private Date duration;
    /** Arrival time. String format. */
    private String arrivalTime;
    /** Duration time. String format. */
    private String durationTime;
    /**
     * @return the data
     */
    public List<BusStopTime[]> getData() {
        return data;
    }
    /**
     * @param data the data to set
     */
    public void setData(List<BusStopTime[]> data) {
        this.data = data;
    }
    /**
     * @return the arrivalDate
     */
    public Date getArrivalDate() {
        return arrivalDate;
    }
    /**
     * @param arrivalDate the arrivalDate to set
     */
    public void setArrivalDate(Date arrivalDate) {
        this.arrivalDate = arrivalDate;
    }
    /**
     * @return the startDate
     */
    public Date getStartDate() {
        return startDate;
    }
    /**
     * @param startDate the startDate to set
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    /**
     * @return the duration
     */
    public Date getDuration() {
        return duration;
    }
    /**
     * @param duration the duration to set
     */
    public void setDuration(Date duration) {
        this.duration = duration;
    }
    /**
     * @return the arrivalTime
     */
    public String getArrivalTime() {
        return arrivalTime;
    }
    /**
     * @param arrivalTime the arrivalTime to set
     */
    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }
    /**
     * @return the durationTime
     */
    public String getDurationTime() {
        return durationTime;
    }
    /**
     * @param durationTime the durationTime to set
     */
    public void setDurationTime(String durationTime) {
        this.durationTime = durationTime;
    }
}
