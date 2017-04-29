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
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ss.sonya.transport.search.SearchEngine;
import ss.sonya.transport.search.vo.SearchSettings;

/**
 *
 * @author ss
 */
public class GraphConstructorTest extends TestConfig {
    @BeforeClass
    public static void initTest() {
        // !!! PRODUCTION DATABASE, ONLY FOR READ
        System.setProperty("catalina.base",
                "/home/ss/kira/apache-tomcat-9.0.0.M19");
    }
    @Autowired
    private SearchEngine searchEngine;
    @Test
    public void test() {
        SearchSettings s = new SearchSettings();
        s.setStartLat(53.88046424318761);
        s.setStartLon(27.43041515350342);
        s.setEndLat(53.88951972656099);
        s.setEndLon(27.46517658233643);
        s.setProfileId(4);
        s.setMaxTransfers(8);
        try {
            searchEngine.search(s);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
