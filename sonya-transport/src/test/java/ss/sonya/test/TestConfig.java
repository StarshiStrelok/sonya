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

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
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
}
