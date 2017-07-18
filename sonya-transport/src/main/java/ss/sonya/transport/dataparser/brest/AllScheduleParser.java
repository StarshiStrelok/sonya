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
package ss.sonya.transport.dataparser.brest;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ss.sonya.entity.BusStop;
import ss.sonya.entity.Path;
import ss.sonya.entity.Route;
import ss.sonya.entity.Trip;
import ss.sonya.inject.service.Geometry;
import ss.sonya.transport.iface.ImportData;
import ss.sonya.transport.api.TransportDataService;
import ss.sonya.transport.component.JsonImportData;
import ss.sonya.transport.component.TransportGeometry;
import ss.sonya.transport.dataparser.RouteType;

/**
 * All Brest transport schedule parser.
 * @author ss
 */
@Component
class AllScheduleParser {
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(AllScheduleParser.class);
    /** Transport profile ID. */
    private static final int TRANSPORT_PROFILE_ID = 1;
    @Autowired
    private TransportDataService transportService;
    @Autowired
    private Geometry geometry;
    @Autowired
    private TransportGeometry transportGeometry;
    private static final Map<Long, Double> trolTimeMap =
            new HashMap<Long, Double>() {{
        put(-538736655L, 35d);  // 1.
        put(1733172090L, 35d);
        put(184718534L, 15d);
        put(-1405645305L, 30d); // 2.
        put(303493943L, 30d);
        put(-867252811L, 37d);    // 3.
        put(-1925570374L, 35d);
        put(-1631320582L, 38d);    // 4.
        put(-851852543L, 40d);
        put(-1089611877L, 5d);
        put(-1004316836L, 38d);      // 5.
        put(458824759L, 38d);
        put(-658290672L, 15d);
        put(20478755L, 20d);
        put(542731938L, 26d);    // 6.
        put(458181067L, 26d);
        put(-658290672L, 15d);
        put(-531181910L, 20d);
        put(1835462151L, 30d);    // 7.
        put(-1371573623L, 27d);
        put(481004234L, 37d);    // 8.
        put(-270287814L, 32d);
        put(-64779760L, 20d);
        put(931209946L, 39d);    // 9.
        put(283171427L, 47d);
        put(-1958533755L, 30d);
    }};
    
    public ImportData parse(RouteType type) throws Exception {
        List<Table> tables = new ArrayList<>();
        tables.addAll(parseXls(type == RouteType.bus ? "autobus.xls" : "alltransport.xls"));
        LOG.info("total tables [" + tables.size() + "]");
        //setAltID(tables);
        return createData(tables, type);
    }
// =============================== PRIVATE ====================================
    private ImportData createData(final List<Table> tables, final RouteType rt)
            throws Exception {
        Map<Long, Path> pathMap = new HashMap<>();
        Map<String, Route> routeMap = new HashMap<>();
        Map<Path, List<Trip>> schedule = new HashMap<>();
        List<BusStop> allBs = transportService
                .getFromProfile(TRANSPORT_PROFILE_ID, BusStop.class);
        Map<Long, BusStop> bsMap = new HashMap<>();
        allBs.stream().forEach(bs -> {
            if (bs.getExternalId() != null) {
                bsMap.put(bs.getExternalId(), bs);
            }
        });
        int fakeId = 0;
        for (Table table : tables) {
            if (table.getType() != rt) {
                continue;
            }
            long externalId;
            if (RouteType.bus == rt) {
                externalId = (table.getName()
                        + "#" + table.getDescription()).hashCode();
            } else {
                externalId = getTrolExternalID(table);
            }
            LOG.info(table.getName() + " / " + table.getDescription() + ": " + externalId);
            Path path;
            if (pathMap.containsKey(externalId)) {
                path = pathMap.get(externalId);
            } else {
                path = new Path();
                path.setId(++fakeId);
                path.setDescription(table.getDescription());

                path.setExternalId(externalId);
                List<BusStop> way = new ArrayList<>();
                for (String[] info : table.getBusstops()) {
                    Long extId = Long.valueOf(info[1]);
                    BusStop exist = bsMap.get(extId);
                    if (exist == null) {
                        LOG.info(table);
                        String msg = "bus stop not found: ext ID [" + extId
                                + "], name [" + info[0] + "]";
                        LOG.warn(msg);
                        throw new IllegalArgumentException(msg);
                    } else {
                        BusStop newBs = new BusStop();
                        newBs.setExternalId(extId);
                        newBs.setLatitude(exist.getLatitude());
                        newBs.setLongitude(exist.getLongitude());
                        newBs.setName(info[0]);
                        way.add(newBs);
                    }
                }
                path.setBusstops(way);
                if (!routeMap.containsKey(table.getName())) {
                    Route route = new Route();
                    String[] split = splitName(table.getName());
                    route.setNamePrefix(split[0]);
                    route.setNamePostfix(
                            table.getType() == RouteType.taxi ? "т" : split[1]);
                    route.setExternalId(Long.parseLong(
                            (table.getName()
                                    + "#" + rt.name()).hashCode() + ""));
                    routeMap.put(table.getName(), route);
                }
                Route route = routeMap.get(table.getName());
                path.setRoute(route);
                pathMap.put(externalId, path);
            }
            // schedule
            if (RouteType.trol == rt) {
                Double time = trolTimeMap.get(path.getExternalId());
                createTripTime(table, path.getBusstops(), time);
                if ("№1".equals(table.getName())
                        || "№2".equals(table.getName())
                        || "№8".equals(table.getName())) {
                    for (BusStop bs : path.getBusstops()) {
                        if (bs.getExternalId() == 132) {
                            bs.setExternalId(332L);
                            LOG.info("fix Trolleybus bug#1");
                        }
                    }
                }
                BusStop firstBs = path.getBusstops().get(0);
                if (firstBs.getExternalId() == 557) {
                    firstBs.setExternalId(558L);
                    LOG.info("fix Trolleybus bug#2");
                }
                BusStop lastBs = path.getBusstops()
                        .get(path.getBusstops().size() - 1);
                if (lastBs.getExternalId() == 558) {
                    lastBs.setExternalId(557L);
                    LOG.info("fix Trolleybus bug#4");
                }
                //LOG.info(table);
            }
            String days = table.getDays();
            List<Trip> trips = new ArrayList<>();
            if (RouteType.taxi == rt) {
                Trip irTrip = new Trip();
                irTrip.setRegular("");
                irTrip.setDays(days);
                JSONArray ta = new JSONArray();
                String sTime = null;
                String eTime = null;
                String prevTime = null;
                for (String[] tripTimes : table.getSchedule()) {
                    if (tripTimes[0].isEmpty() && sTime == null && prevTime != null) {
                        sTime = prevTime;
                    } else if (!tripTimes[0].isEmpty()) {
                        prevTime = tripTimes[0];
                        if (sTime != null && eTime == null) {
                            eTime = prevTime;
                        }
                        ta.put(tripTimes[0]);
                    }
                }
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                if (sTime == null || eTime == null || sdf.parse(sTime).after(sdf.parse(eTime))) {
                    throw new IllegalArgumentException("interval time not found!");
                }
                JSONArray iArr = new JSONArray();
                iArr.put(sTime + "-" + eTime + "=10-12");
                JSONObject o = new JSONObject();
                o.put("interval", iArr);
                o.put("time", ta);
                double km = transportGeometry.calcWayDistance(path.getBusstops());
                double bsTime = path.getBusstops().size() * 0.5;
                double duration = km / 39 * 60 + bsTime;
                o.put("duration", (int) duration);
                LOG.info(o.toString());
                irTrip.setIrregular(o.toString());
                trips.add(irTrip);
            } else {
                for (String[] tripTimes : table.getSchedule()) {
                    StringBuilder sb = new StringBuilder();
                    for (String time : tripTimes) {
                        sb.append(time).append(",");
                    }
                    sb.setLength(sb.length() - 1);
                    Trip trip = new Trip();
                    trip.setPath(path);
                    trip.setRegular(sb.toString());
                    trip.setDays(days);
                    trips.add(trip);
                }
            }
            if (!schedule.containsKey(path)) {
                schedule.put(path, trips);
            } else {
                schedule.get(path).addAll(trips);
            }
        }
        List<Path> pathList = new ArrayList<>(pathMap.values());
        Map<Long, BusStop> mapBs = new HashMap<>();
        pathList.stream().forEach(path -> {
            path.getBusstops().stream().forEach(bs -> {
                mapBs.put(bs.getExternalId(), bs);
            });
        });
        List<BusStop> bsList = new ArrayList<>(/*mapBs.values()*/);
        List<Route> routeList = new ArrayList<>(routeMap.values());
        LOG.info("=> bus stops [" + bsList.size() + "]");
        LOG.info("=> paths [" + pathList.size() + "]");
        LOG.info("=> routes [" + routeList.size() + "]");
        LOG.info("=> schedule [" + schedule.size() + "]");
        return new JsonImportData(bsList, pathList, routeList, schedule);
    }
    /**
     * Parse XLS.
     * @return tables.
     * @throws Exception method error.
     */
    private List<Table> parseXls(final String file) throws Exception {
        LOG.info("------- start parse XLS (" + file + ") --------------------");
        boolean isBus = "autobus.xls".equals(file);
        List<Table> tables = new ArrayList<>();
        InputStream is = getClass().getResourceAsStream(
                "/ss/kira/data/brest/" + file);
        HSSFWorkbook workbook = new HSSFWorkbook(is);
        HSSFSheet sheet = workbook.getSheetAt(0);
        int lastRowNum = sheet.getLastRowNum();
        LOG.info("last row number [" + lastRowNum + "]");
        Row row;
        Cell cell;
        int lastColumnNum;
        String lastRouteType = null;    // end & start table indicator.
        Table table = null;
        String routeName = null;
        String days = null;
        String desc = null;
        List<String[]> busstops = null;
        List<List<String>> schList = null;
        Map<Table, List<String>> depoMap = new HashMap<>();
        for (int i = 2; i < lastRowNum; i++) {
            row = sheet.getRow(i);
            lastColumnNum = row.getLastCellNum();
            LOG.trace("$ column [" + i + "]");
            LOG.trace("last column number [" + lastColumnNum + "]");
            //StringBuilder sb = new StringBuilder();
            for (int j = 0; j < lastColumnNum; j++) {
                cell = row.getCell(j, Row.RETURN_BLANK_AS_NULL);
                String val;
                if (cell == null) {
                    val = "";
                } else {
                    val = cellVal(cell, cell.getCellType()).trim();
                }
                //LOG.info(j + ": " + val);
                if (j == 0) {    // route type
                    if (lastRouteType != null
                            && lastRouteType.isEmpty() && !val.isEmpty()) {
                        // start of table
                        table = new Table();
                        busstops = new ArrayList<>();
                        schList = new ArrayList<>();
                    } else if (lastRouteType != null
                            && !lastRouteType.isEmpty() && val.isEmpty()) {
                        // end of table
                        if (table != null) {
                            table.setBusstops(new ArrayList<>(busstops));
                            table.setName(routeName);
                            table.setDays(days);
                            table.setType(defineRouteType(lastRouteType));
                            table.setDescription(formatDescription(desc));
                            table.setSchedule(rotateSchedule(schList));
                            if (table.getBusstops() == null || routeName == null
                                    || days == null) {
                                throw new IllegalArgumentException(
                                        "corrupted table: " + table);
                            }
                            for (String[] bs : table.getBusstops()) {
                                if (bs[0].isEmpty() || bs[1].isEmpty()) {
                                    throw new IllegalArgumentException(
                                        "corrupted table: " + table);
                                }
                            }
                            if (isBus) {
                                tables.addAll(splitTable(table));
                            } else {
                                tables.add(table);
                            }
                            busstops = null;
                            schList = null;
                            LOG.debug(table.toString());
                        }
                    }
                    if (!val.isEmpty()) {   // inside table
                        schList.add(new ArrayList<>());
                    }
                    lastRouteType = val;
                } else if (j == 1) {        // route name
                    if (!val.isEmpty()) {
                        routeName = val;
                    }
                } else if (j == 2) {        // days
                    if (!val.isEmpty()) {
                        days = convertDays(val);
                    }
                } else if (j == 3) {        // description
                    if (!val.isEmpty()) {
                        desc = val;
                    }
                } else if (j == 4) {        // bus stop name
                    if (busstops != null && !val.isEmpty()) {
                        busstops.add(new String[] {
                            val,
                            null
                        });
                    }
                } else if (j == 5) {        // bus stop alt ID
                    if (busstops != null) {
                        if (val.isEmpty()) {
                            Row nextR;
                            Cell nextCell;
                            int ii = i + 1;
                            while (val.isEmpty()) {
                                nextR = sheet.getRow(ii);
                                if (nextR == null) {
                                    val = "552";        // last table
                                    break;
                                }
                                nextCell = nextR.getCell(
                                        j, Row.RETURN_BLANK_AS_NULL);
                                if (nextCell != null) {
                                    val = cellVal(nextCell,
                                            nextCell.getCellType()).trim();
                                }
                                try {
                                    Long.parseLong(val);
                                } catch (NumberFormatException e) {
                                    val = "";
                                }
                                ii++;
                            }
                        }
                        busstops.get(busstops.size() - 1)[1] = val;
                    }
                } else if (j >= 8) {         // time
                    if ("№15В".equals(routeName) && j == 8 && val.isEmpty()) {
                        LOG.debug("");
                    }
                    if (lastRouteType != null && !lastRouteType.isEmpty()
                            && schList != null) {
                        if (val.isEmpty() || (val.length() != 5
                                && val.length() != 4)) {
                            int jj = j + 1;
                            Cell nextCell;
                            String nVal = "";   // next not empty cell in row
                            while (nVal.isEmpty() && jj < 200) {
                                nextCell = row.getCell(
                                        jj, Row.RETURN_BLANK_AS_NULL);
                                if (nextCell != null) {
                                    nVal = cellVal(nextCell,
                                            nextCell.getCellType()).trim();
                                    // not time
                                    if (nVal.length() != 5
                                            && nVal.length() != 4) {
                                        nVal = "";
                                        break;
                                    }
                                }
                                jj++;
                            }
                            if (!nVal.isEmpty()) {
                                schList.get(schList.size() - 1).add(val);
                            }
                        } else {
                            boolean skip = false;
                            if (cell != null) {
                                short color = cell.getCellStyle()
                                        .getFillForegroundColor();
                                if (color == 13 && ("T".equals(lastRouteType)
                                        || "Т".equals(lastRouteType))) {
                                    if (depoMap.containsKey(table)) {
                                        depoMap.get(table).add(val);
                                    } else {
                                        List<String> l = new ArrayList<>();
                                        l.add(val);
                                        depoMap.put(table, l);
                                    }
                                    skip = true;
                                }
                            }
                            if (!skip) {
                                schList.get(schList.size() - 1).add(val);
                            }
                        }
                    }
                }
                //sb.append(val).append(" | ");
            }
            //LOG.info(sb.toString());
        }
        LOG.info("depo map size [" + depoMap.size() + "]");
        if (!depoMap.isEmpty()) {
            Map<Long, List<String[]>> depoWays = new HashMap<>();
            // 1. Obl.bolnica - depo
            depoWays.put(1733172090L, new ArrayList<String[]>() {{
                add(new String[] {"Областная больница", "404"});
                add(new String[] {"СТО", "284"});
                add(new String[] {"Восточный м-н", "279"});
                add(new String[] {"ФОК", "264"});
                add(new String[] {"Гаврилова", "262"});
                add(new String[] {"ДС Виктория", "291"});
                add(new String[] {"Троллейбусный парк", "227"});
            }});
            // 4. KSM - depo
            depoWays.put(-1631320582L, new ArrayList<String[]>() {{
                add(new String[] {"КСМ", "563"});
                add(new String[] {"Гоздецкого", "562"});
                add(new String[] {"Храм \"Всецарица\"", "560"});
                add(new String[] {"Троллейбусный парк", "227"});
            }});
            // 5. Cvetotron - depo
            depoWays.put(-1004316836L, new ArrayList<String[]>() {{
                add(new String[] {"Цветотрон", "324"});
                add(new String[] {"Центральная", "317"});
                add(new String[] {"28 июля", "315"});
                add(new String[] {"Гребной", "313"});
                add(new String[] {"Завод", "248"});
                add(new String[] {"Беларусбанк", "167"});
                add(new String[] {"Ковры Бреста", "246"});
                add(new String[] {"Чулочный", "244"});
                add(new String[] {"Рембыттехника", "241"});
                add(new String[] {"Автошкола", "238"});
                add(new String[] {"Пожарное депо", "237"});
                add(new String[] {"Савушкин продукт", "555"});
                add(new String[] {"ДС Виктория", "291"});
                add(new String[] {"Троллейбусный парк", "227"});
            }});
            // 5. Obl.bolnica - depo
            depoWays.put(458824759L, new ArrayList<String[]>() {{
                add(new String[] {"Областная больница", "404"});
                add(new String[] {"СТО", "284"});
                add(new String[] {"Восточный м-н", "279"});
                add(new String[] {"ФОК", "264"});
                add(new String[] {"Гаврилова", "262"});
                add(new String[] {"ДС Виктория", "291"});
                add(new String[] {"Троллейбусный парк", "227"});
            }});
            // 6. Obl.bolnica - depo
            depoWays.put(458181067L, new ArrayList<String[]>() {{
                add(new String[] {"Областная больница", "404"});
                add(new String[] {"СТО", "284"});
                add(new String[] {"Восточный м-н", "279"});
                add(new String[] {"ФОК", "264"});
                add(new String[] {"Гаврилова", "262"});
                add(new String[] {"ДС Виктория", "291"});
                add(new String[] {"Троллейбусный парк", "227"});
            }});
            // 6. Cvetotron - depo
            depoWays.put(542731938L, new ArrayList<String[]>() {{
                add(new String[] {"Цветотрон", "324"});
                add(new String[] {"Ковалево", "325"});
                add(new String[] {"Пугачево", "364"});
                add(new String[] {"Заречная", "298"});
                add(new String[] {"Гузнянская", "297"});
                add(new String[] {"Крушинская", "295"});
                add(new String[] {"Парк", "277"});
                add(new String[] {"ФОК", "264"});
                add(new String[] {"Гаврилова", "262"});
                add(new String[] {"ДС Виктория", "291"});
                add(new String[] {"Троллейбусный парк", "227"});
            }});
            // 8. Cvetotron - depo
            depoWays.put(481004234L, new ArrayList<String[]>() {{
                add(new String[] {"Цветотрон", "324"});
                add(new String[] {"Центральная", "317"});
                add(new String[] {"28 июля", "315"});
                add(new String[] {"Гребной", "313"});
                add(new String[] {"Завод", "247"});
                add(new String[] {"Богданчука", "249"});
                add(new String[] {"Технический университет", "362"});
                add(new String[] {"Парк", "277"});
                add(new String[] {"ФОК", "264"});
                add(new String[] {"Гаврилова", "262"});
                add(new String[] {"ДС Виктория", "291"});
                add(new String[] {"Троллейбусный парк", "227"});
            }});
            // 9. Uzny - depo
            depoWays.put(283171427L, new ArrayList<String[]>() {{
                add(new String[] {"ДС Южный", "133"});
                add(new String[] {"Южный", "132"});
                add(new String[] {"Машиностроительный завод", "322"});
                add(new String[] {"Цветотрон", "319"});
                add(new String[] {"Ковалево", "325"});
                add(new String[] {"Пугачево", "364"});
                add(new String[] {"Заречная", "298"});
                add(new String[] {"Гузнянская", "297"});
                add(new String[] {"Крушинская", "295"});
                add(new String[] {"Парк", "277"});
                add(new String[] {"ФОК", "264"});
                add(new String[] {"Гаврилова", "262"});
                add(new String[] {"ДС Виктория", "291"});
                add(new String[] {"Троллейбусный парк", "227"});
            }});
            tables.addAll(splitDepoTable(depoMap, depoWays));
        }
        LOG.info("tables [" + tables.size() + "]");
        LOG.info("------- parsing complete ----------------------------------");
        return tables;
    }
    private List<Table> splitDepoTable(Map<Table, List<String>> depoMap,
            Map<Long, List<String[]>> depoWays) {
        List<Table> list = new ArrayList<>();
        for (Table t : depoMap.keySet()) {
            Table nTable = new Table();
            nTable.setType(RouteType.trol);
            nTable.setName(t.getName());
            nTable.setDescription(t.getDescription() + " (в депо)");
            nTable.setBusstops(depoWays.get(getTrolExternalID(t)));
            nTable.setDays(t.getDays());
            List<String> times = depoMap.get(t);
            String[][] schedule =
                    new String[times.size()][nTable.getBusstops().size()];
            int counter = 0;
            for (String time : times) {
                schedule[counter][0] = time;
                counter++;
            }
            nTable.setSchedule(schedule);
            list.add(nTable);
        }
        return list;
    }
    
    private List<Table> splitTable(final Table table) throws Exception {
        List<Table> result = new ArrayList<>();
        Map<List<String[]>, List<List<String>>> map = new HashMap<>();
        for (String[] trip : table.getSchedule()) {
            StringBuilder sb = new StringBuilder();
            List<String[]> bsList = new ArrayList<>();
            List<String> tripList = new ArrayList<>();
            int idx = 0;
            for (String time : trip) {
                if (time == null || time.trim().isEmpty()) {
                    sb.append("0");
                } else {
                    bsList.add(table.getBusstops().get(idx));
                    tripList.add(time);
                    sb.append("1");
                }
                idx++;
            }
            String s = sb.toString();
            if (s.contains("0")) {
                while (s.startsWith("0")) {
                    s = s.substring(1);
                }
                while (s.endsWith("0")) {
                    s = s.substring(0, s.length() -1);
                }
            }
            boolean isShort = s.contains("0");
            if (isShort) {
                LOG.debug("split table: " + table);
            } else {
                bsList = table.getBusstops();
                tripList = Arrays.asList(trip);
            }
            if (map.containsKey(bsList)) {
                map.get(bsList).add(tripList);
            } else {
                List<List<String>> l = new ArrayList<>();
                l.add(tripList);
                map.put(bsList, l);
            }
        }
        if (map.size() > 1) {
            int counter = 0;
            for (List<String[]> bs : map.keySet()) {
                Table t = new Table();
                t.setBusstops(bs);
                t.setDays(table.getDays());
                StringBuilder d = new StringBuilder(table.getDescription());
                for (int i = 0; i < counter; i++) {
                    d.append("'");
                }
                t.setDescription(d.toString());
                t.setName(table.getName());
                t.setType(table.getType());
                List<List<String>> sch = map.get(bs);
                String[][] schedule = new String[sch.size()][];
                for (int i = 0; i < sch.size(); i++) {
                    schedule[i] = sch.get(i).toArray(new String[0]);
                }
                t.setSchedule(schedule);
                LOG.info("splitted table: " + t);
                result.add(t);
                counter++;
            }
        } else {
            result.add(table);
        }
        return result;
    }
    /**
     * Extract excel cell value.
     * @param cell - cell.
     * @param type - cell type.
     * @return - cell value.
     */
    private String cellVal(final Cell cell, final int type) {
        if (type == Cell.CELL_TYPE_NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                return new DataFormatter().formatRawCellContents(
                        cell.getNumericCellValue(), type, "H:MM;@");
            } else {
                return String.valueOf((int) (cell.getNumericCellValue()));
            }
        } else if (type == Cell.CELL_TYPE_STRING) {
            return cell.getStringCellValue();
        } else if (type == Cell.CELL_TYPE_FORMULA) {
            return cellVal(cell, cell.getCachedFormulaResultType());
        } else {
            return "";
        }
    }
    /**
     * Define route type.
     * @param val type string.
     * @return route type.
     */
    private RouteType defineRouteType(final String val) {
        if ("A".equals(val) || "А".equals(val)) {
            return RouteType.bus;
        } else if ("T".equals(val) || "Т".equals(val)) {
            return RouteType.trol;
        } else if ("M".equals(val) || "М".equals(val)) {
            return RouteType.taxi;
        } else {
            throw new IllegalArgumentException("unknown route type ["
                    + val + "]");
        }
    }
    /**
     * Convert day string.
     * @param daysStr - day string.
     * @return - days as Calendar integer.
     * @throws Exception - method error.
     */
    private String convertDays(final String daysStr) throws Exception {
        if ("Р".equals(daysStr)) {
            return "2,3,4,5,6";
        } else if ("Р,В".equals(daysStr) || "Р, В".equals(daysStr)
                || "раб. и  вых. дни".equals(daysStr)) {
            return "1,2,3,4,5,6,7";
        } else if ("В".equals(daysStr)) {
            return "1,7";
        } else if ("ПН, СР, ПТ".equals(daysStr)) {
            return "2,4,6";
        } else if ("ВТ, ЧТ, СБ, ВС".equals(daysStr)) {
            return "3,5,7,1";
        } else if ("Р,СБ".equals(daysStr) || "Р, СБ".equals(daysStr)) {
            return "2,3,4,5,6,7";
        } else if ("ВСК".equals(daysStr)) {
            return "1";
        } else if ("СБ".equals(daysStr)) {
            return "7";
        } else if ("раб. дни и сб.".equals(daysStr)) {
            return "2,3,4,5,6,7";
        } else {
            throw new IllegalArgumentException(
                    "unknown day string [" + daysStr + "]");
        }
    }
    /**
     * Rotate schedule.
     * @param list old schedule format.
     * @return new schedule format.
     */
    private String[][] rotateSchedule(final List<List<String>> list) {
        int tripsCount = Integer.MIN_VALUE;
        for (List<String> bsSchedule : list) {
            if (tripsCount < bsSchedule.size()) {
                tripsCount = bsSchedule.size();
            }
        }
        String[][] schedule = new String[tripsCount][list.size()];
        int i = 0;
        for (List<String> bsSchedule : list) {
            int j = 0;
            for (String time : bsSchedule) {
                time = time.length() == 4 ? "0" + time : time;
                schedule[j][i] = time;
                j++;
            }
            i++;
        }
        return schedule;
    }
    /**
     * Split table route name.
     * @param name table route name.
     * @return prefix & postfix.
     */
    private String[] splitName(final String name) {
        String clean = name.replace("№", "");
        char[] ch = clean.toCharArray();
        int idx = -1;
        for (int i = 0; i < ch.length; i++) {
            try {
                Integer.parseInt(ch[i] + "");
            } catch (NumberFormatException ex) {
                idx = i;
                break;
            }
        }
        String f, s;
        if (idx == -1) {
            f = clean;
            s = null;
        } else {
            f = clean.substring(0, idx);
            s = clean.substring(idx);
        }
        return new String[] {
            f, s
        };
    }
    /**
     * Format table description.
     * @param desc table description.
     * @return formatted description.
     */
    private String formatDescription(final String desc) {
        return desc.replace("Свердл.", "Свердлова")
                .replace("Кам.Жиров.", "Каменица-Жировецкая")
                .replace("Гор.больница №1", "Городская больница № 1")
                .replace("Гор.больн.№1", "Городская больница № 1")
                .replace("Гор. больница №1", "Городская больница № 1")
                .replace("Обл. больн.", "Областная больница")
                .replace("Обл.больн", "Областная больница")
                .replace("ППВ", "Пункт подготовки вагонов")
                .replace("Варш.рынок", "рынок \"Варшавский\"")
                .replace("Гост.Дружба", "Гостиница \"Дружба\"")
                .replace("АП", "Автобусный парк №1")
                .replace("Приг. вокзал", "Пригородный вокзал")
                .replace("Газоапп.", "Газоаппарат")
                .replace("М.р-н Южный", "ДП \"Южный городок\"")
                .replace("Б.Зап", "Брест-Западный")
                .replace("Брест Зап.", "Брест-Западный")
                .replace("Газоаап.", "Газоаппарат")
                .replace("БОАТ", "Брестоблавтотранс")
                .replace("Стр.рынок", "Строительный рынок")
                .replace("Санта Бремор", "Санта-Бремор")
                .replace("ЦГБ", "Центральная городская больница")
                .replace("Областная больницаица", "Областная больница");
    }

    private void createTripTime(final Table table, final List<BusStop> way,
            final double time) throws Exception {
        double distance = transportGeometry.calcWayDistance(way);   // km
        Calendar calendar = new GregorianCalendar();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        for (String[] tripTime : table.getSchedule()) {
            String startTime = tripTime[0];
            Date startDate = sdf.parse(startTime);
            calendar.setTime(startDate);
            Iterator<BusStop> itr = way.iterator();
            BusStop prev = null;
            int counter = 1;
            while (itr.hasNext()) {
                BusStop bs = itr.next();
                if (prev == null) {
                    prev = bs;
                    continue;
                }
                double subDist = geometry.calcDistance(prev.getLatitude(),
                        prev.getLongitude(), bs.getLatitude(),
                        bs.getLongitude());                     // km
                double subTime = subDist / distance * time * 60;     // sec
                //subTime += 60;  // +1 min for bus stop
                calendar.add(Calendar.SECOND, (int) subTime);
                String bsTime = sdf.format(calendar.getTime());
                LOG.debug(bs.getName() + " --> " + bsTime);
                if (tripTime[counter] != null && !tripTime[counter].isEmpty()) {
                    calendar.setTime(sdf.parse(tripTime[counter]));
                } else {
                    tripTime[counter] = bsTime;
                }
                prev = bs;
                counter++;
            }
        }
    }
    private Long getTrolExternalID(Table table) {
        StringBuilder buf = new StringBuilder();
        table.getBusstops().stream().forEach(inf -> {
            buf.append(inf[0]).append(",");
        });
        if ("Областная больница".equals(table.getBusstops().get(0)[0])
                && table.getDescription().contains("депо")) {
            buf.append(table.getDescription());
        }
        long extid = (buf.toString()).hashCode();
        return extid;
    }
}
