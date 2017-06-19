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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ss.sonya.constants.RegistrationStatus;
import ss.sonya.constants.UserRole;
import ss.sonya.entity.UserProfile;
import ss.sonya.inject.CommonDAO;
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
    /** Basic authentication. */
    private static final String AUTH_BASIC = "Basic";
    /** User profile DAO. */
    @Autowired
    private UserProfileDAO userProfileDAO;
    /** Common DAO. */
    @Autowired
    private CommonDAO commonDAO;
    /** Password encoder. */
    @Resource
    private PasswordEncoder passwordEncoder;
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
    @Override
    public RegistrationStatus createProfile(final UserProfile profile) {
        LOG.info("---------- create new user profile ------------------------");
        try {
            UserProfile exist = userProfileDAO.findByLogin(profile.getLogin());
            if (exist != null) {
                return RegistrationStatus.DUPLICATE;
            }
            profile.setCreated(new Date());
            profile.setRole(UserRole.ROLE_ADMIN);
            profile.setPassword(passwordEncoder.encode(profile.getPassword()));
            commonDAO.create(profile);
            return RegistrationStatus.CREATED;
        } catch (Exception e) {
            LOG.error("user profile creation error!", e);
            return RegistrationStatus.ERROR;
        }
    }
    @Override
    public Authentication authentication(final String token)
            throws UsernameNotFoundException, BadCredentialsException {
        LOG.trace("authentication header [" + token + "]");
        if (token.startsWith(AUTH_BASIC)) {
            String base64 = token.replace(AUTH_BASIC, "").trim();
            String decoded;
            try {
                decoded = new String(Base64.getMimeDecoder().decode(base64),
                        "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                LOG.warn("authentication token encoding error!");
                throw new IllegalArgumentException(
                        "authentication token encoding error!");
            }
            String[] pair = decoded.split(":");
            String login = pair[0];
            LOG.trace("login [" + login + "]");
            String password = pair[1];
            UserProfile profile = userProfileDAO.findByLogin(login);
            if (profile == null) {
                throw new UsernameNotFoundException(
                        "user profile not found in DB!");
            }
            UserDetails user = createSpringUser(profile);
            if (user == null || !user.getUsername().equals(login)) {
                throw new UsernameNotFoundException("login not found!");
            }
            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new BadCredentialsException("password not match!");
            }
            Collection<? extends GrantedAuthority> authorities =
                    user.getAuthorities();
            return new UsernamePasswordAuthenticationToken(user,
                    password, authorities);
        } else {
            return null;
        }
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
