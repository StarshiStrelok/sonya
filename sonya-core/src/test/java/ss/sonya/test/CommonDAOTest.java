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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ss.sonya.constants.UserRole;
import ss.sonya.entity.UserProfile;
import ss.sonya.inject.DataService;

/**
 * Common DAO test.
 * @author ss
 */
public class CommonDAOTest extends TestConfig {
    /** Common DAO. */
    @Autowired
    private DataService dataService;
    @Test
    public void test() throws Exception {
        UserProfile up = createValid();
        UserProfile up2 = createValid();
        UserProfile up3 = createValid();
        List<UserProfile> createList = new ArrayList<>();
        createList.add(up2);
        createList.add(up3);
        up = dataService.create(up);
        Integer id = up.getId();
        up = dataService.findById(up.getId(), UserProfile.class);
        Assert.assertNotNull(up);
        dataService.createAll(createList);
        List<UserProfile> all = dataService.getAll(UserProfile.class);
        Assert.assertTrue(all.size() == 3);
        String newLogin = "New test login";
        up.setLogin(newLogin);
        up = dataService.update(up);
        up = dataService.findById(up.getId(), UserProfile.class);
        Assert.assertEquals(up.getLogin(), newLogin);
        String newLogin2 = "New test login 2";
        up.setLogin(newLogin2);
        List<UserProfile> list = new ArrayList<>();
        list.add(up);
        dataService.updateAll(list);
        up = dataService.findById(id, UserProfile.class);
        Assert.assertEquals(up.getLogin(), newLogin2);
        dataService.delete(id, UserProfile.class);
        up = dataService.findById(id, UserProfile.class);
        Assert.assertNull(up);
        all = dataService.getAll(UserProfile.class);
        Assert.assertTrue(all.size() == 2);
        dataService.deleteAll(all);
        all = dataService.getAll(UserProfile.class);
        Assert.assertTrue(all.size() == 0);
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
