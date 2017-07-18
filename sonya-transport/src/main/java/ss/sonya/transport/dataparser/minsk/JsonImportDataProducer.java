/*
 * Copyright (C) 2017 ss
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package ss.sonya.transport.dataparser.minsk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ss.sonya.entity.BusStop;
import ss.sonya.entity.Path;
import ss.sonya.entity.Route;
import ss.sonya.entity.Trip;
import ss.sonya.transport.iface.ImportData;
import ss.sonya.transport.component.JsonImportData;
import ss.sonya.transport.dataparser.RouteType;

/**
 * JSON import data producer.
 * @author ss
 */
@Component
class JsonImportDataProducer {
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(
            JsonImportDataProducer.class);
    /** Bus stop parser. */
    @Autowired
    private BusStopCSVParser busstopParser;
    /** Routes parser. */
    @Autowired
    private RouteCSVParser routeParser;
    /** Schedule parser for autobus, tram and trolleybus. */
    @Autowired
    private CSVTimeParser timeParser;
    /** Metro schedule parser. */
    @Autowired
    private MetroScheduleDP sdpMetro;
    /**
     * Create import data.
     * @param rt route type.
     * @return JSON import data.
     * @throws Exception operation error.
     */
    public ImportData createData(RouteType rt) throws Exception {
        // parse bus stops
        List<BusStop> busstops = busstopParser.parse();
        LOG.info("==> busstops [" + busstops.size() + "]");
        Map<Long, BusStop> bsMap = new HashMap<>();
        for (BusStop bs : busstops) {
            bsMap.put(bs.getExternalId(), bs);
        }
        
        // routes and paths
        List<Route> routes = routeParser.parse(busstops, rt);
        LOG.info("==> routes [" + routes.size() + "]");
        List<Path> paths = new ArrayList<>();
        for (Route route : routes) {
            paths.addAll(route.getPaths());
            String key = route.getNamePrefix()
                    + (route.getNamePostfix() == null
                    ? "" : route.getNamePostfix())
                    + "#" + rt.name();
            route.setExternalId((long) key.hashCode());
        }
        for (Path p : paths) {
            for (BusStop bs : p.getBusstops()) {
                if (bsMap.containsKey(bs.getExternalId())) {
                    bsMap.remove(bs.getExternalId());
                }
            }
        }
        LOG.info("==> losted bus stops [" + bsMap.size() + "]");
        busstops.removeAll(bsMap.values());
        LOG.info("==> busstops [" + busstops.size() + "]");
        LOG.info("==> paths [" + paths.size() + "]");
        Map<Path, List<Trip>> sch;
        if (rt == RouteType.metro) {
            sch = sdpMetro.extract(routes);
        } else {
            sch = timeParser.extract(routes);
        }
        LOG.info("==> total schedule [" + sch.size() + "]");
        ImportData importData = new JsonImportData(
                busstops, paths, routes, sch);
        return importData;
    }
}
