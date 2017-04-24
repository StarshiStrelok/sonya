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
import java.util.List;

/**
 * Data service.
 * Define common data methods here.
 * @author ss
 */
public interface DataService {
    /**
     * Create entity.
     * @param <T> entity class.
     * @param entity entity.
     * @return created entity.
     * @throws Exception error.
     */
    <T> T create(T entity) throws Exception;
    /**
     * Update entity.
     * @param <T> - entity class.
     * @param entity - entity.
     * @return - updated entity.
     * @throws Exception error.
     */
    <T> T update(T entity) throws Exception;
    /**
     * Find entity by ID.
     * @param <T> - entity type.
     * @param id - entity ID.
     * @param cl - entity class.
     * @return - entity.
     * @throws Exception error.
     */
    <T> T findById(Serializable id, Class<T> cl) throws Exception;
    /**
     * Delete entity.
     * @param <T> - entity type.
     * @param id - entity ID.
     * @param cl - entity class.
     * @throws Exception error.
     */
    <T> void delete(Serializable id, Class<T> cl) throws Exception;
    /**
     * Get all entities.
     * @param <T> entity type.
     * @param cl entity class.
     * @return list entities.
     * @throws Exception error.
     */
    <T> List<T> getAll(Class<T> cl) throws Exception;
    /**
     * Create entities.
     * @param <T> entity type.
     * @param entities list entities.
     * @throws Exception error.
     */
    <T> void createAll(List<T> entities) throws Exception;
    /**
     * Update entities.
     * @param <T> entity type.
     * @param entities list entities.
     * @throws Exception error.
     */
    <T> void updateAll(List<T> entities) throws Exception;
    /**
     * Delete entities.
     * @param <T> entity type.
     * @param entities list entities.
     * @throws Exception error.
     */
    <T> void deleteAll(List<T> entities) throws Exception;
}
