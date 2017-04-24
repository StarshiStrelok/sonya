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
import ss.sonya.entity.Path;
import ss.sonya.entity.Route;
import ss.sonya.entity.Trip;

/**
 * Transport data service API.
 * @author ss
 */
public interface TransportDataService {
    /**
     * Get all entities from transport profile.
     * @param <T> entity with transport profile field.
     * @param id transport profile ID.
     * @param cl entity class.
     * @return list entities.
     * @throws Exception error.
     */
    <T> List<T> getFromProfile(Integer id, Class<T> cl) throws Exception;
    /**
     * Get routes from same route profile (type).
     * @param id route profile ID.
     * @return routes from same type.
     * @throws Exception error.
     */
    List<Route> getRoutesFromSameType(Integer id) throws Exception;
    /**
     * Get all paths from same route.
     * @param id route ID.
     * @return all route paths.
     * @throws Exception error.
     */
    List<Path> getPathsFromRoute(Integer id) throws Exception;
    /**
     * Get path schedule.
     * @param id path ID.
     * @return schedule.
     * @throws Exception error.
     */
    List<Trip> getSchedule(Integer id) throws Exception;
}
