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
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ss.sonya.entity.Path;
import ss.sonya.inject.rest.DataWS;
import ss.sonya.transport.api.TransportDataService;

/**
 * Path web-service.
 * @author ss
 */
@RestController
@RequestMapping("/rest/data/path")
public class PathWS extends DataWS<Path> {
    /** Transport service. */
    @Autowired
    private TransportDataService transportService;
    /**
     * Initialize controller.
     */
    @PostConstruct
    protected void init() {
        type = Path.class;
    }
    /**
     * Get all paths from same route.
     * @param id route ID.
     * @return list paths from same route.
     * @throws Exception error.
     */
    @RequestMapping(value = "/from-route/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<Path> getPathsFromRoute(
            @PathVariable("id") Integer id) throws Exception {
        return transportService.getPathsFromRoute(id);
    }
    @RequestMapping(method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Override
    public Path update(@RequestBody Path path) throws Exception {
        path.setSchedule(transportService.getSchedule(path.getId()));
        return dataService.update(path);
    }
}
