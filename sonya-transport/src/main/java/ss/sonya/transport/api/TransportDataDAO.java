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
import ss.sonya.entity.Route;

/**
 * Transport data DAO API.
 * @author ss
 */
public interface TransportDataDAO {
    /**
     * Get all entities from transport profile.
     * @param <T> entity.
     * @param id transport profile ID.
     * @param cl entity class.
     * @return list entities.
     */
    <T> List<T> getFromProfile(Integer id, Class<T> cl);
    /**
     * Get routes from same route profile.
     * @param id route profile ID.
     * @return list routes.
     */
    List<Route> getRoutesFromSameType(Integer id);
}
