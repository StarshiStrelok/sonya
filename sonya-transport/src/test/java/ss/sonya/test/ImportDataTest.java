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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import ss.sonya.entity.BusStop;
import ss.sonya.entity.Path;
import ss.sonya.entity.Route;
import ss.sonya.entity.TransportProfile;
import ss.sonya.entity.Trip;
import ss.sonya.transport.api.ImportDataService;
import ss.sonya.transport.component.ImportDataEvent;
import ss.sonya.transport.component.JsonDataSerializer;
import ss.sonya.transport.component.JsonImportData;
import ss.sonya.transport.dataparser.DataParser;
import ss.sonya.transport.iface.ImportData;

/**
 *
 * @author ss
 */
public class ImportDataTest extends TestConfig {
    @Autowired
    private JsonDataSerializer serializer;
    @Autowired
    private ImportDataService importDataService;
    @Autowired @Qualifier("BrestAutobus")
    private DataParser dataParser;
    @Test
    @Ignore
    public void test() throws Exception {
        TransportProfile tp = createValidTransportProfile();
        tp = dataService.create(tp);
        List<BusStop> bsList = new ArrayList<>();
        List<Path> pathList = new ArrayList<>();
        List<Route> routeList = new ArrayList<>();
        Map<Path, List<Trip>> schedule = new HashMap<>();
        ImportData data = new JsonImportData(bsList, pathList,
                routeList, schedule);
        // fill data
        long timestamp = System.currentTimeMillis();
        for (int i = 0; i < 5; i++) {
            BusStop bs = new BusStop();
            bs.setName("Point#" + i);
            bs.setExternalId(timestamp + i);
            bs.setLatitude(34.23);
            bs.setLongitude(55.23);
            bsList.add(bs);
        }
        Route route = new Route();
        route.setExternalId(timestamp - 100L);
        route.setNamePrefix("2");
        route.setNamePostfix("x");
        routeList.add(route);
        Path path = new Path();
        path.setRoute(route);
        path.setBusstops(bsList);
        path.setDescription("test way");
        path.setExternalId(timestamp - 200L);
        pathList.add(path);
        // create import data
        byte[] binData = serializer.serialize(data);
        MultipartFile file = new MockMultipartFile("file", binData);
        List<ImportDataEvent> events = importDataService.importData(
                file.getBytes(), tp.getId(),
                tp.getRouteProfiles().iterator().next().getId(), false, false);
        Assert.assertTrue(!events.isEmpty());
    }
    @Test
    @Ignore
    public void test2() throws Exception {
        dataParser.parse();
    }
}
