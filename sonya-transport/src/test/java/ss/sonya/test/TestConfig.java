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

import java.util.Arrays;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ss.sonya.entity.RouteProfile;
import ss.sonya.entity.TransportProfile;
import ss.sonya.inject.DataService;
import ss.sonya.transport.config.TransportSpringConfig;

/**
 * Test configuration.
 * @author ss
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TransportSpringConfig.class)
public abstract class TestConfig {
    @BeforeClass
    public static void initTest() {
        // put test sonya.properties to /home/ss/kira/testing/conf
        System.setProperty("catalina.base", "/home/ss/kira/testing");
    }
    @Autowired
    protected DataService dataService;
    protected TransportProfile createValidTransportProfile() {
        TransportProfile tp = new TransportProfile();
        tp.setCenterLat(30.0);
        tp.setCenterLon(30.0);
        tp.setInitialZoom(12);
        tp.setMinZoom(12);
        tp.setName("test profile");
        tp.setNorthEastLat(32.0);
        tp.setNorthEastLon(32.0);
        tp.setSouthWestLat(28.0);
        tp.setSouthWestLon(28.0);
        tp.setHasSchedule(false);
        tp.setBusStopAccessZoneRadius(0.3);
        tp.setSearchLimitForPoints(6);
        RouteProfile rp = new RouteProfile();
        rp.setAvgSpeed(30d);
        rp.setLineColor("#343434");
        rp.setName("test route type");
        rp.setTransportProfile(tp);
        rp.setRoutingURL("http://example.com");
        tp.setRouteProfiles(Arrays.asList(new RouteProfile[] {rp}));
        return tp;
    }
}
