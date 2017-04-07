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
package ss.sonya.inject;

import java.io.Serializable;

/**
 * Common DAO interface.
 * @author ss
 */
public interface CommonDAO {
    /**
     * Create entity.
     * @param <T> - entity class.
     * @param entity - entity.
     * @return - created entity.
     */
    <T> T create(final T entity);
    /**
     * Update entity.
     * @param <T> - entity class.
     * @param entity - entity.
     * @return - updated entity.
     */
    <T> T update(final T entity);
    /**
     * Find entity by ID.
     * @param <T> - entity type.
     * @param id - entity ID.
     * @param cl - entity class.
     * @return - entity.
     */
    <T> T findById(final Serializable id, Class<T> cl);
    /**
     * Delete entity.
     * @param <T> - entity type.
     * @param id - entity ID.
     * @param cl - entity class.
     */
    <T> void delete(final Serializable id, Class<T> cl);
}
