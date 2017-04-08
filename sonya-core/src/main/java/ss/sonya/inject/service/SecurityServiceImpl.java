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
package ss.sonya.inject.service;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ss.sonya.constants.UserRole;
import ss.sonya.entity.UserProfile;
import ss.sonya.inject.SonyaSecurity;
import ss.sonya.inject.UserProfileDAO;

/**
 * Security service implementation.
 * @author ss
 */
@Service
public class SecurityServiceImpl implements SonyaSecurity {
    /** Logger. */
    private static final Logger LOG = Logger
            .getLogger(SecurityServiceImpl.class);
    /** User profile DAO. */
    @Autowired
    private UserProfileDAO userProfileDAO;
    @Override
    public UserDetails loadUserByUsername(final String login)
            throws UsernameNotFoundException {
        UserProfile profile = userProfileDAO.findByLogin(login);
        if (profile == null) {
            throw new UsernameNotFoundException(
                    "user profile not found in DB!");
        }
        return createSpringUser(profile);
    }
// ============================= PRIVATE ======================================
    /**
     * Create spring User from database user profile.
     * @param profile - user profile.
     * @return - spring user.
     */
    private User createSpringUser(final UserProfile profile) {
        UserRole userRole = profile.getRole();
        GrantedAuthority ga = new SimpleGrantedAuthority(userRole.name());
        List<GrantedAuthority> list = new ArrayList<>();
        list.add(ga);
        for (UserRole child : userRole.getChildRoles()) {
            GrantedAuthority childGA = new SimpleGrantedAuthority(child.name());
            list.add(childGA);
        }
        return new User(profile.getLogin(), profile.getPassword(),
                true, true, true, true, list);
    }
}
