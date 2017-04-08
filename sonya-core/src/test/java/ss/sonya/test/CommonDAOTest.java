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

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ss.sonya.entity.BusStop;
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
        BusStop bs = createValidBusStop();
        bs = commonDAO.create(bs);
        bs = commonDAO.findById(bs.getId(), BusStop.class);
        Assert.assertNotNull(bs);
        String newName = "New test bus stop name";
        bs.setName(newName);
        bs = commonDAO.update(bs);
        bs = commonDAO.findById(bs.getId(), BusStop.class);
        Assert.assertEquals(bs.getName(), newName);
        Integer id = bs.getId();
        commonDAO.delete(id, BusStop.class);
        bs = commonDAO.findById(id, BusStop.class);
        Assert.assertNull(bs);
    }
    private BusStop createValidBusStop() {
        BusStop bs = new BusStop();
        bs.setLatitude(53.2345);
        bs.setLongitude(29.2344);
        bs.setName("Test bus stop");
        return bs;
    }
}
