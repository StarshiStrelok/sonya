/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ss.sonya.transport.dataparser.minsk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import ss.sonya.entity.BusStop;
import ss.sonya.entity.Path;
import ss.sonya.entity.Route;
import ss.sonya.transport.dataparser.RouteType;

/**
 * Route CSV parser.
 * @author ss
 */
@Component
class RouteCSVParser {
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(RouteCSVParser.class);
    /** CSV resource. */
    private static final String FILE = "/ss/kira/data/minsk/routes.csv";
    /**
     * Parse CSV.
     * @param bsList all bus stops.
     * @param rt route type.
     * @return routes.
     */
    public List<Route> parse(final List<BusStop> bsList, RouteType rt) {
        LOG.info("######### start parsing... route type [" + rt + "]");
        Map<Long, BusStop> bsMap = new HashMap<>();
        Map<String, Route> routeMap = new HashMap<>();
        for (BusStop bs : bsList) {
            bsMap.put(bs.getExternalId(), bs);
        }
        int skipped = 0;
        int routes = 0;
        int fakeId = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                    getClass().getResourceAsStream(FILE)))) {
            String line;
            String lastName = "";
            String lastType = "";
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] row = line.split(";");
                if ("RouteNum".equals(row[0])) {
                    continue;
                }
                if (row[0].isEmpty()) {
                    row[0] = lastName;
                }
                if (row[3].isEmpty()) {
                    row[3] = lastType;
                }
                LOG.debug("path [ name=" + row[0] + ", type=" + row[3]
                        + ", desc=" + formatDesc(row[10]) + ", id=" + row[12]
                        + ", bus stops=" + row[14] + " ]");
                if (row[14].trim().isEmpty()) {
                    skipped++;
                    continue;
                }
                if (!row[0].isEmpty()) {
                    lastName = row[0];
                    lastType = row[3];
                    Path path = new Path();
                    path.setId(++fakeId);
                    path.setExternalId(Long.valueOf(row[12]));
                    path.setDescription(formatDesc(row[10]));
                    if (152643 == path.getExternalId()) {
                        continue;
                    }
                    List<BusStop> pathBs = new ArrayList<>();
                    for (String bsId : row[14].split(",")) {
                        BusStop b = bsMap.get(Long.valueOf(bsId));
                        if (b == null) {
                            throw new IllegalArgumentException("bus stop ["
                                    + bsId + "] not found");
                        } else {
                            pathBs.add(b);
                        }
                    }
                    if (pathBs.isEmpty()) {
                        throw new IllegalArgumentException("empty path");
                    }
                    path.setBusstops(pathBs);
                    String routeName = row[0];
                    String routeType = row[3];
                    String key = routeName + ":::" + routeType;
                    if (routeMap.containsKey(key)) {
                        path.setRoute(routeMap.get(key));
                        routeMap.get(key).getPaths().add(path);
                    } else {
                        Route route = new Route();
                        route.setPaths(new ArrayList<>());
                        RouteType type = RouteType.valueOf(row[3]);
                        if (type == null) {
                            throw new IllegalArgumentException("type ["
                                    + routeType + "] not found");
                        }
                        if (type != rt) {
                            continue;
                        }
                        String[] rnSplit;
                        if (type == RouteType.metro) {
                            rnSplit = new String[] {
                                routeName, ""
                            };
                        } else {
                            rnSplit = splitRouteName(routeName);
                        }
                        route.setNamePrefix(rnSplit[0]);
                        route.setNamePostfix(rnSplit[1]);
                        routeMap.put(key, route);
                        route.getPaths().add(path);
                        path.setRoute(route);
                    }
                } else {
                    throw new IllegalArgumentException("wrong line ["
                            + line + "]");
                }
            }
        } catch (Exception ex) {
            LOG.error("parse CSV error!", ex);
        }
        LOG.info("### routes created --> " + routeMap.size());
        LOG.info("### skipped [" + skipped + "], routes [" + routes
                + "]");
        LOG.info("######### complete...");
        return new ArrayList<>(routeMap.values());
    }
    /**
     * Split route name.
     * @param str - route name.
     * @return - prefix and postfix.
     * @throws IOException - parsing error.
     */
    private String[] splitRouteName(final String str) throws IOException {
        String[] result = new String[2];
        int idx = 0;
        for (Character ch : str.toCharArray()) {
            if (!Character.isDigit(ch)) {
                break;
            }
            idx++;
        }
        if (idx == 0) {
            throw new IOException("wrong string [" + str + "]");
        } else {
            result[0] = str.substring(0, idx);
            result[1] = str.substring(idx);
            LOG.debug("prefix [" + result[0] + "] postfix [" + result[1] + "]");
        }
        return result;
    }
    /**
     * Format path description.
     * @param str - description.
     * @return - formatted description.
     */
    private String formatDesc(final String str) {
        return str.replace("D", "Депо");
    }
}
