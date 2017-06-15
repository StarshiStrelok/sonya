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
package ss.sonya.transport.search.vo;

import ss.sonya.entity.BusStop;

/**
 * Graph search decision.
 * @author ss
 */
public class Decision {
    /** Path from start to end. */
    private final Integer[] way;
    /** Start vertex start bus stop. */
    private final BusStop s;
    /** End vertex end bus stop. */
    private final BusStop e;
    /**
     * Constructor.
     * @param pS - start vertex start bus stop.
     * @param pE - end vertex end bus stop.
     * @param pWay - path from start vertex to end vertex.
     */
    public Decision(final BusStop pS, final BusStop pE,
            final Integer[] pWay) {
        s = pS;
        e = pE;
        way = pWay;
    }
    /**
     * @return the path
     */
    public Integer[] getWay() {
        return way;
    }
    /**
     * @return the s
     */
    public BusStop getS() {
        return s;
    }
    /**
     * @return the e
     */
    public BusStop getE() {
        return e;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int c : way) {
            sb.append(c).append("#");
        }
        sb.append(s.getId()).append("#").append(e.getId());
        return sb.toString();
    }
}
