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
package ss.sonya.transport.api;

import java.util.List;
import java.util.Map;
import ss.sonya.entity.BusStop;
import ss.sonya.entity.Path;
import ss.sonya.entity.Route;
import ss.sonya.entity.Trip;

/**
 * Import data API.
 * @author ss
 */
public interface ImportData {
    /**
     * Bus stops.
     * @return - bus stops.
     */
    List<BusStop> busstops();
    /**
     * Paths.
     * @return - paths.
     */
    List<Path> paths();
    /**
     * Routes.
     * @return - routes.
     */
    List<Route> routes();
    /**
     * Schedule.
     * @return - schedule.
     */
    Map<Path, List<Trip>> schedule();
}
