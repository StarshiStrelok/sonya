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
package ss.sonya.inject.service;

import org.springframework.stereotype.Service;

/**
 * Geometry component.
 * @author ss
 */
@Service
public class Geometry {
    /** Earth radius, km. */
    private static final double EARTH_RAD = 6371;
    /**
     * Calculate distance between coordinates.
     * @param lat1 - latitude point 1.
     * @param lng1 - longitude point 1.
     * @param lat2 - latitude point 2.
     * @param lng2 - longitude point 2.
     * @return - straight distance, km.
     */
    public double calcDistance(final Double lat1, final Double lng1,
            final Double lat2, final Double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lng2 - lng1);
        double rLat1 = Math.toRadians(lat1);
        double rLat2 = Math.toRadians(lat2);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.sin(dLon / 2) * Math.sin(dLon / 2)
                * Math.cos(rLat1) * Math.cos(rLat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RAD * c;
    }
}
