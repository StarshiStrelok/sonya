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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ss.sonya.constants.RegistrationStatus;
import ss.sonya.entity.UserProfile;
import ss.sonya.inject.SonyaSecurity;

/**
 * Transport security rest controller.
 * @author ss
 */
@RestController
@RequestMapping("/rest/transport/security")
public class SecurityRESTCtrl {
    /** Security service. */
    @Autowired
    private SonyaSecurity security;
    /**
     * Get exist profiles count.
     * @return profiles count.
     */
    @RequestMapping(value = "/profiles-count", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public int profilesCount() {
        return security.profilesCount();
    }
    /**
     * Create new profile.
     * @param profile profile.
     * @return registration status.
     */
    @RequestMapping(value = "/new-account", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public RegistrationStatus createProfile(
            @RequestBody final UserProfile profile) {
        return security.createProfile(profile);
    }
}
