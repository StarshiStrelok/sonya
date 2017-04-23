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
package ss.sonya.transport.component;

import java.util.List;
import java.util.Map;
import ss.sonya.entity.BusStop;
import ss.sonya.entity.Path;
import ss.sonya.entity.Route;
import ss.sonya.entity.RouteProfile;
import ss.sonya.entity.TransportProfile;
import ss.sonya.entity.Trip;
import ss.sonya.transport.api.ImportData;

/**
 * JSON import data.
 * @author ss
 */
public class JsonImportData implements ImportData {
    /** Bus stops. */
    private final List<BusStop> busstops;
    /** Paths. */
    private final List<Path> paths;
    /** Routes. */
    private final List<Route> routes;
    /** Schedule. */
    private final Map<Path, List<Trip>> schedule;
    /** Transport profile. */
    private final TransportProfile transportProfile;
    /** Route profile. */
    private final RouteProfile routeProfile;
    /**
     * Constructor.
     * @param lBs bus stops.
     * @param lP paths.
     * @param lR routes.
     * @param sch schedule.
     * @param tP transport profile.
     * @param rP route profile.
     */
    public JsonImportData(final List<BusStop> lBs, final List<Path> lP,
            final List<Route> lR, final Map<Path, List<Trip>> sch,
            final TransportProfile tP, final RouteProfile rP) {
        busstops = lBs;
        paths = lP;
        routes = lR;
        schedule = sch;
        transportProfile = tP;
        routeProfile = rP;
    }
    @Override
    public List<BusStop> busstops() {
        return busstops;
    }
    @Override
    public List<Path> paths() {
        return paths;
    }
    @Override
    public List<Route> routes() {
        return routes;
    }
    @Override
    public Map<Path, List<Trip>> schedule() {
        return schedule;
    }
    @Override
    public TransportProfile transportProfile() {
        return transportProfile;
    }
    @Override
    public RouteProfile routeProfile() {
        return routeProfile;
    }
}
