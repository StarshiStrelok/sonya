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

import javax.annotation.PostConstruct;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ss.sonya.entity.RouteProfile;
import ss.sonya.entity.TransportProfile;
import ss.sonya.inject.rest.DataWS;

/**
 * Transport profile web-service.
 * @author ss
 */
@RestController
@RequestMapping("/rest/data/transport-profile")
public class TransportProfileWS extends DataWS<TransportProfile> {
    /**
     * Initialize controller.
     */
    @PostConstruct
    protected void init() {
        type = TransportProfile.class;
    }
    /**
     * Update transport profile.
     * @param profile transport profile.
     * @return updated profile.
     * @throws Exception error.
     */
    @Override
    @RequestMapping(method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public TransportProfile update(
            @RequestBody TransportProfile profile) throws Exception {
        for (RouteProfile rp : profile.getRouteProfiles()) {
            rp.setTransportProfile(profile);
        }
        return dataService.update(profile);
    }
    /**
     * Create transport profile.
     * @param profile transport profile.
     * @return created profile.
     * @throws Exception error.
     */
    @Override
    @RequestMapping(method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public TransportProfile create(@RequestBody TransportProfile profile)
            throws Exception {
        for (RouteProfile rp : profile.getRouteProfiles()) {
            rp.setTransportProfile(profile);
        }
        return dataService.create(profile);
    }
}
