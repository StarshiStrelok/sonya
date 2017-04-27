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

import java.util.List;
import ss.sonya.entity.Path;

/**
 * Optimal path.
 * Intermediate search result.
 * Contains set of paths and it's ways.
 * @author ss
 */
public class OptimalPath {
    /** Native path. */
    private List<Path> path;
    /** Way time. */
    private Double time;
    /** Optimal Schedule. */
    private OptimalSchedule schedule;
    /**
     * @return the path
     */
    public List<Path> getPath() {
        return path;
    }
    /**
     * @param path the path to set
     */
    public void setPath(List<Path> path) {
        this.path = path;
    }
    /**
     * @return the time
     */
    public Double getTime() {
        return time;
    }
    /**
     * @param time the time to set
     */
    public void setTime(Double time) {
        this.time = time;
    }
    /**
     * @return the schedule
     */
    public OptimalSchedule getSchedule() {
        return schedule;
    }
    /**
     * @param schedule the schedule to set
     */
    public void setSchedule(OptimalSchedule schedule) {
        this.schedule = schedule;
    }
}
