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
package ss.sonya.test;

import java.util.Date;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ss.sonya.constants.UserRole;
import ss.sonya.entity.UserProfile;
import ss.sonya.inject.CommonDAO;

/**
 * Common DAO test.
 * @author ss
 */
public class CommonDAOTest extends TestConfig {
    /** Common DAO. */
    @Autowired
    private CommonDAO commonDAO;
    @Test
    public void test() {
        UserProfile up = createValid();
        up = commonDAO.create(up);
        up = commonDAO.findById(up.getId(), UserProfile.class);
        Assert.assertNotNull(up);
        String newLogin = "New test login";
        up.setLogin(newLogin);
        up = commonDAO.update(up);
        up = commonDAO.findById(up.getId(), UserProfile.class);
        Assert.assertEquals(up.getLogin(), newLogin);
        Integer id = up.getId();
        commonDAO.delete(id, UserProfile.class);
        up = commonDAO.findById(id, UserProfile.class);
        Assert.assertNull(up);
    }
    private UserProfile createValid() {
        UserProfile up = new UserProfile();
        up.setCreated(new Date());
        up.setLogin("Test login");
        up.setPassword("******");
        up.setRole(UserRole.ROLE_ADMIN);
        return up;
    }
}
