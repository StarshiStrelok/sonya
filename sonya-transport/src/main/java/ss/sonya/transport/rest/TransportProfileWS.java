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
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ss.sonya.entity.TransportProfile;
import ss.sonya.inject.rest.DataWS;
import ss.sonya.transport.api.TransportDataService;

/**
 * Transport profile web-service.
 * @author ss
 */
@RestController
@RequestMapping("/rest/data/transport-profile")
public class TransportProfileWS extends DataWS<TransportProfile> {
    /** Transport data service. */
    @Autowired
    private TransportDataService transportService;
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
        profile.getRouteProfiles().forEach((rp) -> {
            rp.setTransportProfile(profile);
        });
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
        profile.getRouteProfiles().forEach((rp) -> {
            rp.setTransportProfile(profile);
        });
        return dataService.create(profile);
    }
    /**
     * Upload bus stop marker image for route type.
     * @param id route type ID.
     * @param file uploaded image.
     * @throws Exception error.
     */
    @RequestMapping(value = "/route/marker/{id}", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void uploadRouteTypeBusStopMarker(
            @PathVariable("id") Integer id,
            @RequestBody MultipartFile file) throws Exception {
        transportService.uploadRouteTypeBusStopMarker(id, file);
    }
    /**
     * Get route type bus stop marker image.
     * @param id route profile ID.
     * @param resp HTTP servlet response.
     * @throws Exception error.
     */
    @RequestMapping(value = "/route/marker/{id}", method = RequestMethod.GET,
            produces = MediaType.IMAGE_PNG_VALUE)
    public void getRouteTypeBusStopMarker(@PathVariable("id") Integer id,
            HttpServletResponse resp) throws Exception {
        byte[] data = transportService.getRouteTypeBusStopMarker(id);
        if (data != null && data.length > 0) {
            resp.getOutputStream().write(data);
        }
    }
}
