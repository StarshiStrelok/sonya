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
package ss.sonya.transport.rest;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ss.sonya.inject.rest.DataWS;
import ss.sonya.transport.api.TransportDataService;

/**
 * Transport common web service.
 * @author ss
 * @param <T> entity class.
 */
public abstract class TransportWS<T> extends DataWS<T> {
    /** Transport data service. */
    @Autowired
    private TransportDataService transportService;
    /**
     * Get all entities from same transport profile.
     * @param id transport profile ID.
     * @return all found entities from same transport profile.
     * @throws Exception error.
     */
    @RequestMapping(value = "/from-profile/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<T> getFromProfile(
            @PathVariable("id") Integer id) throws Exception {
        return transportService.getFromProfile(id, type);
    }
}