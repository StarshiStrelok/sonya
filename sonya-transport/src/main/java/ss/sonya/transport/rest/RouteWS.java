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
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ss.sonya.entity.Route;

/**
 * Route web-service.
 * @author ss
 */
@RestController
@RequestMapping("/rest/data/route")
public class RouteWS extends TransportWS<Route> {
    /**
     * Initialize controller.
     */
    @PostConstruct
    protected void init() {
        type = Route.class;
    }
    /**
     * Get routes from same route profile.
     * @param id route profile ID.
     * @return list routes.
     * @throws Exception error.
     */
    @RequestMapping(value = "/from-type/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<Route> getFromRouteType(
            @PathVariable("id") Integer id) throws Exception {
        return transportService.getRoutesFromSameType(id);
    }
}
