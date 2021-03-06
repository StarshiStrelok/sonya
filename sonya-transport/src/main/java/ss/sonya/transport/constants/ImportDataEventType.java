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
package ss.sonya.transport.constants;

/**
 * Import data event type.
 * @author ss
 */
public enum ImportDataEventType {
    /** Create new bus stop. */
    BUS_STOP_CREATE,
    /** Update bus stop. */
    BUS_STOP_UPDATE,
    /** Create new route. */
    ROUTE_CREATE,
    /** Update route. */
    ROUTE_UPDATE,
    /** Create new path. */
    PATH_CREATE,
    /** Update path. */
    PATH_UPDATE,
    /** Path way changed. */
    PATH_WAY_CHANGED,
    /** Path schedule changed. */
    PATH_SCHEDULE_CHANGED,
    /** Delete orphan path. */
    DELETE_ORPHAN_PATH,
    /** Delete orphan route. */
    DELETE_ORPHAN_ROUTE,
    /** Delete orphan bus stop. */
    DELETE_ORPHAN_BUS_STOP
}
