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
package ss.sonya.transport.dataparser.brest;

import java.util.List;
import ss.sonya.transport.dataparser.RouteType;

/**
 * One XLS table.
 * @author ss
 */
class Table {
    /** Route type. */
    private RouteType type;
    /** Route name. */
    private String name;
    /** Route description. */
    private String description;
    /** Days. */
    private String days;
    /** Bus stops, alternative ID / bus stop name. */
    private List<String[]> busstops;
    /** Schedule. */
    private String[][] schedule;
// ======================================= SET & GET ==========================
    /**
     * @return the type
     */
    public RouteType getType() {
        return type;
    }
    /**
     * @param type the type to set
     */
    public void setType(RouteType type) {
        this.type = type;
    }
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
    /**
     * @return the days
     */
    public String getDays() {
        return days;
    }
    /**
     * @param days the days to set
     */
    public void setDays(String days) {
        this.days = days;
    }
    /**
     * @return the busstops
     */
    public List<String[]> getBusstops() {
        return busstops;
    }
    /**
     * @param busstops the busstops to set
     */
    public void setBusstops(List<String[]> busstops) {
        this.busstops = busstops;
    }
    /**
     * @return the schedule
     */
    public String[][] getSchedule() {
        return schedule;
    }
    /**
     * @param schedule the schedule to set
     */
    public void setSchedule(String[][] schedule) {
        this.schedule = schedule;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Table [ ").append("type=").append(type).append(", name=")
                .append(name).append(", desc=").append(description)
                .append(", days=").append(days).append(" ]\n");
        for (String[] bs : busstops) {
            sb.append(String.format("%-28s", bs[0])).append(":")
                    .append(bs[1]).append("\n");
        }
        sb.append("\nSchedule\n");
        for (int i = 0; i < schedule[0].length; i++) {
            sb.append("+-----");
        }
        sb.append("+\n");
        String format = "|%-5s";
        for (String[] trip : schedule) {
            for (String time : trip) {
                sb.append(String.format(format, time == null ? "" : time));
            }
            sb.append("|\n");
        }
        for (int i = 0; i < schedule[0].length; i++) {
            sb.append("+-----");
        }
        sb.append("+\n");
        return sb.toString();
    }
}
