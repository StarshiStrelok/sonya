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
package ss.sonya.inject.rest;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ss.sonya.inject.DataService;

/**
 * Data web-service.
 * @author ss
 * @param <T> model class.
 */
public abstract class DataWS<T> {
    /** Data service. */
    @Autowired
    private DataService dataService;
    /** Controller type. */
    protected Class<T> type;
    /**
     * Create entity.
     * @param entity entity.
     * @return created entity.
     * @throws Exception error.
     */
    @RequestMapping(method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public T create(@RequestBody T entity) throws Exception {
        return dataService.create(entity);
    }
    /**
     * Update entity.
     * @param entity entity.
     * @return updated entity.
     * @throws Exception error.
     */
    @RequestMapping(method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public T update(
            @RequestBody T entity) throws Exception {
        return dataService.update(entity);
    }
    /**
     * Delete entity.
     * @param id entity ID.
     * @throws Exception error.
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void delete(@PathVariable("id") Integer id)
            throws Exception {
        dataService.delete(id, type);
    }
    /**
     * Get entity by ID.
     * @param id entity ID.
     * @return entity.
     * @throws Exception error.
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object get(@PathVariable("id") Integer id) throws Exception {
        return dataService.findById(id, type);
    }
    /**
     * Get all entities.
     * @return all entities.
     * @throws Exception error.
     */
    @RequestMapping(value = "/all", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List getAll()
            throws Exception {
        return dataService.getAll(type);
    }
}
