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

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;
import ss.sonya.entity.BusStop;
import ss.sonya.entity.Path;

/**
 * Optimal path.
 * Intermediate search result.
 * Contains set of paths and it's ways.
 * @author ss
 */
public class OptimalPath {
    /** Paths. */
    private List<Path> path;
    /** Path ways. */
    private List<List<BusStop>> way;
    /** Duration of trip. */
    private Double time;
    /** Distance, km. */
    private Double distance;
    /** Optimal Schedule. */
    private OptimalSchedule schedule;
    /** Decision. */
    private Decision decision;
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
    /**
     * @return the way
     */
    public List<List<BusStop>> getWay() {
        return way;
    }
    /**
     * @param way the way to set
     */
    public void setWay(List<List<BusStop>> way) {
        this.way = way;
    }
    /**
     * @return the distance
     */
    public Double getDistance() {
        return distance;
    }
    /**
     * @param distance the distance to set
     */
    public void setDistance(Double distance) {
        this.distance = distance;
    }
    /**
     * @return the decision
     */
    public Decision getDecision() {
        return decision;
    }
    /**
     * @param decision the decision to set
     */
    public void setDecision(Decision decision) {
        this.decision = decision;
    }
    @Override
    public String toString() {
        long minInHour = TimeUnit.HOURS.toMinutes(1);
        String format = "| %-29s | %-8d | %-8d |\n";
        DecimalFormat df = new DecimalFormat("###.#");
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");
        String hr = "+-------------------------------+----------+----------+\n";
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("==========================================================");
        sb.append("\n");
        sb.append("> Optimal path <\n");
        sb.append("> transfers [").append(path.size() - 1).append("]\n");
        sb.append("> decision [");
        for (int i = 0; i < decision.getWay().length; i++) {
            sb.append(decision.getWay()[i]).append("-");
        }
        sb.setLength(sb.length() - 1);
        sb.append("]\n");
        if (time != null) {
            sb.append("> time [").append(df.format(time * minInHour))
                    .append("] min\n");
        }
        if (schedule != null) {
            sb.append("> arrival time [")
                    .append(sdfTime.format(schedule.getArrivalDate()))
                    .append("]\n");
            sb.append("> trip duration [")
                    .append(sdfTime.format(schedule.getDuration()))
                    .append("]\n");
        }
        sb.append("\n");
        path.forEach(p -> {
            sb.append("\t").append(p.getRoute().getType().getName());
            sb.append(": ").append(p.getRoute().getNamePrefix());
            if (p.getRoute().getNamePostfix() != null) {
                sb.append(p.getRoute().getNamePostfix());
            }
            sb.append(" | ").append(p.getDescription()).append("\n");
            if (schedule != null) {
                BusStopTime[] t = schedule.getData().get(path.indexOf(p));
                sb.append("\ttrip time: ")
                        .append(sdfTime.format(t[0].getTime()))
                        .append(" - ").append(sdfTime.format(t[1].getTime()))
                        .append("\n");
            }
            sb.append(hr);
            sb.append("|           Bus stop            "
                    + "|    ID    | External |\n");
            sb.append(hr);
            way.get(path.indexOf(p)).forEach(bs -> {
                sb.append(String.format(format,
                        bs.getName(), bs.getId(), bs.getExternalId()));
            });
            sb.append(hr).append("\n");
        });
        sb.append("==========================================================");
        sb.append("\n");
        return sb.toString();
    }
}
