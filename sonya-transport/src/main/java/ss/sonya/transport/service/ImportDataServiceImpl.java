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
package ss.sonya.transport.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ss.sonya.constants.TransportConst;
import ss.sonya.entity.BusStop;
import ss.sonya.entity.Path;
import ss.sonya.entity.Route;
import ss.sonya.entity.RouteProfile;
import ss.sonya.entity.TransportProfile;
import ss.sonya.entity.Trip;
import ss.sonya.inject.DataService;
import ss.sonya.transport.iface.ImportData;
import ss.sonya.transport.api.ImportDataSerializer;
import ss.sonya.transport.api.ImportDataService;
import ss.sonya.transport.api.TransportDataService;
import ss.sonya.transport.component.ImportDataEvent;
import ss.sonya.transport.constants.ImportDataEventType;
import ss.sonya.transport.constants.ImportInfoKey;
import ss.sonya.transport.exception.EmptyFieldException;
import ss.sonya.transport.exception.ImportDataException;
import ss.sonya.transport.iface.ExternalRef;

/**
 * Import data service implementation.
 * @author ss
 */
@Service
class ImportDataServiceImpl implements ImportDataService {
    /** Logger. */
    private static final Logger LOG = Logger
            .getLogger(ImportDataService.class);
    /** Data serializer. */
    @Autowired
    private ImportDataSerializer serializer;
    /** Data service. */
    @Autowired
    private DataService dataService;
    /** Transport service. */
    @Autowired
    private TransportDataService transportService;
    @Override
    public List<ImportDataEvent> importData(final MultipartFile file,
            final Integer tpId, final Integer rtID, final boolean isPersist)
            throws ImportDataException {
        List<ImportDataEvent> events = new ArrayList<>();
        try {
            TransportProfile tProfile = dataService
                    .findById(tpId, TransportProfile.class);
            RouteProfile rProfile = dataService
                    .findById(rtID, RouteProfile.class);
            ImportData data = serializer.deserialize(file.getBytes(),
                    tProfile, rProfile);
            // ---------------------- bus stops -------------------------------
            List<BusStop> busstops = data.busstops();
            if (busstops != null && !busstops.isEmpty()) {
                events.addAll(handleBusStops(busstops, isPersist, tpId));
            }
            // ---------------------- routes ----------------------------------
            List<Route> routes = data.routes();
            if (routes != null && !routes.isEmpty()) {
                events.addAll(handleRoutes(routes, isPersist, tpId));
            }
            // ---------------------- paths -----------------------------------
            List<Path> paths = data.paths();
            if (paths != null && !paths.isEmpty()) {
                events.addAll(handlePaths(paths, isPersist, tpId));
            }
            // ---------------------- schedule --------------------------------
            Map<Path, List<Trip>> sch = data.schedule();
            if (sch != null && !sch.isEmpty()) {
                events.addAll(handleSchedule(sch, isPersist, tpId));
            }
            if (isPersist) {
            // ---------------------- orphan routes & paths -------------------
                events.addAll(deleteOrphanRoutesAndPaths(
                        paths, routes, tpId, rtID));
            }
            return events;
        } catch (Exception e) {
            LOG.error("import data error!", e);
            throw new ImportDataException("import data error!", e);
        }
    }
// ================================== PRIVATE =================================
    /**
     * Delete orphan paths & routes.
     * @param paths actual paths.
     * @param routes actual routes.
     * @param profileId transport profile ID.
     * @param rid route profile ID.
     * @return events.
     * @throws Exception method error.
     */
    private List<ImportDataEvent> deleteOrphanRoutesAndPaths(
            final List<Path> paths, final List<Route> routes,
            final Integer profileId, final Integer rid) throws Exception {
        List<ImportDataEvent> events = new ArrayList<>();
        List<Route> allRoutes = transportService
                .getFromProfile(profileId, Route.class);
        Map<Long, Route> routeMap = new HashMap<>();
        Map<Long, Path> pathMap = new HashMap<>();
        for (Route r : allRoutes) {
            if (r.getExternalId() != null && rid.equals(r.getType().getId())) {
                routeMap.put(r.getExternalId(), r);
            }
        }
        for (Route route : routes) {
            Route exist = routeMap.get(route.getExternalId());
            routeMap.remove(route.getExternalId());
            if (exist == null) {
                continue;
            }
            List<Path> existPaths = transportService
                    .getPathsFromRoute(exist.getId());
            for (Path p : existPaths) {
                if (p.getExternalId() != null) {
                    pathMap.put(p.getExternalId(), p);
                }
            }
        }
        for (Path path : paths) {
            pathMap.remove(path.getExternalId());
        }
        for (Path del : pathMap.values()) {
            events.add(createEvent(del,
                    ImportDataEventType.DELETE_ORPHAN_PATH,
                    new HashMap<>()
            ));
        }
        for (Route del : routeMap.values()) {
            events.add(createEvent(del,
                    ImportDataEventType.DELETE_ORPHAN_ROUTE,
                    new HashMap<>()
            ));
        }
        LOG.info("orphan paths [" + pathMap.values().size() + "]");
        LOG.info("orphan routes [" + routeMap.values().size() + "]");
        dataService.deleteAll(new ArrayList<>(pathMap.values()));
        dataService.deleteAll(new ArrayList<>(routeMap.values()));
        return events;
    }
    /**
     * Handle schedule.
     * @param sch schedule.
     * @param isPersist persist changes flag.
     * @param profileId transport profile ID.
     * @return events.
     * @throws Exception method error.
     */
    private List<ImportDataEvent> handleSchedule(
            final Map<Path, List<Trip>> sch, final boolean isPersist,
            final Integer profileId) throws Exception {
        List<ImportDataEvent> events = new ArrayList<>();
        List<Path> update = new ArrayList<>();
        Map<Long, Path> pathMap = createFullMap(Path.class, profileId);
        for (Path p : sch.keySet()) {
            if (p.getExternalId() == null) {
                throw new EmptyFieldException("externalId", p);
            }
            Path path = pathMap.get(p.getExternalId());
            if (path == null) {
                continue;       // path not exist else
            }
            List<Trip> persist = transportService.getSchedule(path.getId());
            List<Trip> schedule = sch.get(p);
            boolean hasChanges = false;
            if (schedule.size() != persist.size()) {
                hasChanges = true;
            } else {
                for (int i = 0; i < schedule.size(); i++) {
                    Trip trip1 = schedule.get(i);
                    Trip trip2 = persist.get(i);
                    String key1 = trip1.getDays() + "#" + trip1.getRegular()
                            + "#" + trip1.getIrregular();
                    String key2 = trip2.getDays() + "#" + trip2.getRegular()
                            + "#" + trip2.getIrregular();
                    if (!key1.equals(key2)) {
                        hasChanges = true;
                        break;
                    }
                }
            }
            if (hasChanges) {
                schedule.stream().forEach(t -> {
                    t.setPath(path);
                });
                path.setSchedule(schedule);
                update.add(path);
                events.add(createEvent(path,
                        ImportDataEventType.PATH_SCHEDULE_CHANGED,
                        new HashMap<>()
                ));
            }
        }
        LOG.info("update schedules [" + update.size() + "]");
        if (isPersist && !update.isEmpty()) {
            dataService.updateAll(update);
        }
        return events;
    }
    /**
     * Handle paths.
     * @param paths paths.
     * @param isPersist persist changes flag.
     * @param profileId transport profile ID.
     * @return events.
     * @throws Exception method error.
     */
    private List<ImportDataEvent> handlePaths(final List<Path> paths,
            final boolean isPersist, final Integer profileId) throws Exception {
        List<ImportDataEvent> events = new ArrayList<>();
        List<Path> create = new ArrayList<>();
        List<Path> update = new ArrayList<>();
        Map<Long, BusStop> bsMap = createFullMap(BusStop.class, profileId);
        Map<Long, Path> pathMap = createFullMap(Path.class, profileId);
        Map<Long, Route> routeMap = createFullMap(Route.class, profileId);
        for (Path path : paths) {
            if (path.getExternalId() == null) {
                throw new EmptyFieldException("externalId", path);
            }
            if (path.getRoute() == null
                    || path.getRoute().getExternalId() == null) {
                throw new EmptyFieldException("route", path);
            }
            if (path.getBusstops() == null || path.getBusstops().isEmpty()) {
                throw new EmptyFieldException("busstops", path);
            }
            Path persist = pathMap.get(path.getExternalId());
            List<BusStop> pathBusStops = new ArrayList<>();
            for (BusStop bs : path.getBusstops()) {
                if (bs.getExternalId() == null) {
                    throw new EmptyFieldException("externalId", bs);
                }
                if (!bsMap.containsKey(bs.getExternalId())) {
                    throw new IllegalArgumentException("bus stop with "
                            + "external ID [" + bs.getExternalId()
                            + "] not found!");
                }
                pathBusStops.add(bsMap.get(bs.getExternalId()));
            }
            if (persist == null) {
                path.setBusstops(pathBusStops);
                path.setRoute(routeMap.get(path.getRoute().getExternalId()));
                create.add(path);
                events.add(createEvent(path, ImportDataEventType.PATH_CREATE,
                        new HashMap<>()
                ));
            } else {
                List<ImportDataEvent> updEvents = new ArrayList<>();
                if (!persist.getDescription().equals(path.getDescription())) {
                    updEvents.add(createEvent(persist,
                            ImportDataEventType.PATH_UPDATE,
                        new HashMap<ImportInfoKey, String>() { {
                            put(ImportInfoKey.FIELD, "description");
                            put(ImportInfoKey.OLD_VALUE,
                                    persist.getDescription());
                            put(ImportInfoKey.NEW_VALUE,
                                    path.getDescription());
                        } }
                    ));
                    persist.setDescription(path.getDescription());
                }
                List<BusStop> persistBusStops = new ArrayList<>();
                persistBusStops.addAll(persist.getBusstops());
                List<BusStop> mocks = persistBusStops.stream().filter(
                        bs -> TransportConst.MOCK_BS.equals(bs.getName()))
                        .collect(Collectors.toList());
                persistBusStops.removeAll(mocks);
                boolean wayChanged = false;
                if (persistBusStops.size() != pathBusStops.size()) {
                    wayChanged = true;
                } else {
                    for (int i = 0; i < pathBusStops.size(); i++) {
                        if (!pathBusStops.get(i)
                                .equals(persistBusStops.get(i))) {
                            wayChanged = true;
                            break;
                        }
                    }
                }
                if (!mocks.isEmpty()) {
                    persistBusStops = persist.getBusstops();
                    for (BusStop mock : mocks) {
                        int preMockIdx = persistBusStops.indexOf(mock) - 1;
                        if (preMockIdx < 0) {
                            continue;
                        }
                        BusStop preMockBs = persistBusStops.get(preMockIdx);
                        int preMockIdx2 = pathBusStops.indexOf(preMockBs);
                        if (preMockIdx2 < 0) {
                            continue;
                        }
                        pathBusStops.add(preMockIdx2 + 1, mock);
                    }
                }
                if (wayChanged) {
                    persist.setBusstops(pathBusStops);
                    updEvents.add(createEvent(persist,
                            ImportDataEventType.PATH_WAY_CHANGED,
                        new HashMap<>()
                    ));
                }
                if (!updEvents.isEmpty()) {
                    update.add(persist);
                    events.addAll(updEvents);
                }
            }
        }
        LOG.info("create paths [" + create.size() + "]");
        LOG.info("update paths [" + update.size() + "]");
        if (isPersist) {
            dataService.createAll(create);
            dataService.updateAll(update);
        }
        return events;
    }
    /**
     * Handle routes.
     * @param routes routes.
     * @param isPersist persist changes flag.
     * @param profileId transport profile ID.
     * @return events.
     * @throws Exception error.
     */
    private List<ImportDataEvent> handleRoutes(final List<Route> routes,
            final boolean isPersist, final Integer profileId) throws Exception {
        List<ImportDataEvent> events = new ArrayList<>();
        List<Route> create = new ArrayList<>();
        List<Route> update = new ArrayList<>();
        Map<Long, Route> allMap = createFullMap(Route.class, profileId);
        for (Route route : routes) {
            if (route.getExternalId() == null) {
                throw new EmptyFieldException("externalId", route);
            }
            Route persist = allMap.get(route.getExternalId());
            if (persist == null) {
                create.add(route);
                events.add(createEvent(route, ImportDataEventType.ROUTE_CREATE,
                        new HashMap<>()
                ));
            } else {
                List<ImportDataEvent> updEvents = new ArrayList<>();
                String persistName = persist.getNamePrefix()
                        + (persist.getNamePostfix() == null
                        ? "" : persist.getNamePostfix());
                String routeName = route.getNamePrefix()
                        + (route.getNamePostfix() == null
                        ? "" : route.getNamePostfix());
                if (!persistName.equals(routeName)) {
                    updEvents.add(createEvent(persist,
                            ImportDataEventType.ROUTE_UPDATE,
                        new HashMap<ImportInfoKey, String>() { {
                            put(ImportInfoKey.FIELD, "name");
                            put(ImportInfoKey.OLD_VALUE, persistName);
                            put(ImportInfoKey.NEW_VALUE, routeName);
                        } }
                    ));
                    persist.setNamePostfix(route.getNamePostfix());
                    persist.setNamePrefix(route.getNamePrefix());
                }
                if (!updEvents.isEmpty()) {
                    update.add(persist);
                    events.addAll(updEvents);
                }
            }
        }
        LOG.info("create routes [" + create.size() + "]");
        LOG.info("update routes [" + update.size() + "]");
        if (isPersist) {
            dataService.createAll(create);
            dataService.updateAll(update);
        }
        return events;
    }
    /**
     * Handle bus stops data.
     * @param busstops bus stops list.
     * @param isPersist persist changes flag.
     * @param profileId transport profile ID.
     * @return events.
     * @throws Exception method error.
     */
    private List<ImportDataEvent> handleBusStops(final List<BusStop> busstops,
            final boolean isPersist, final Integer profileId) throws Exception {
        List<ImportDataEvent> events = new ArrayList<>();
        List<BusStop> create = new ArrayList<>();
        List<BusStop> update = new ArrayList<>();
        Map<Long, BusStop> allMap = createFullMap(BusStop.class, profileId);
        for (BusStop bs : busstops) {
            if (bs.getExternalId() == null) {
                throw new EmptyFieldException("externalId", bs);
            }
            BusStop persistBs = allMap.get(bs.getExternalId());
            if (persistBs == null) {
                // new bus stop
                create.add(bs);
                events.add(createEvent(bs, ImportDataEventType.BUS_STOP_CREATE,
                        new HashMap<>()
                ));
            } else {
                // exist bus stop
                List<ImportDataEvent> updEvents = new ArrayList<>();
                if (!persistBs.getName().equals(bs.getName())) {
                    updEvents.add(createEvent(persistBs,
                            ImportDataEventType.BUS_STOP_UPDATE,
                        new HashMap<ImportInfoKey, String>() { {
                            put(ImportInfoKey.FIELD, "name");
                            put(ImportInfoKey.OLD_VALUE, persistBs.getName());
                            put(ImportInfoKey.NEW_VALUE, bs.getName());
                        } }
                    ));
                    persistBs.setName(bs.getName());
                }
                if (!persistBs.getLatitude().equals(bs.getLatitude())) {
                    updEvents.add(createEvent(persistBs,
                            ImportDataEventType.BUS_STOP_UPDATE,
                        new HashMap<ImportInfoKey, String>() { {
                            put(ImportInfoKey.FIELD, "latitude");
                            put(ImportInfoKey.OLD_VALUE,
                                    persistBs.getLatitude() + "");
                            put(ImportInfoKey.NEW_VALUE, bs.getLatitude() + "");
                        } }
                    ));
                    persistBs.setLatitude(bs.getLatitude());
                }
                if (!persistBs.getLongitude().equals(bs.getLongitude())) {
                    updEvents.add(createEvent(persistBs,
                            ImportDataEventType.BUS_STOP_UPDATE,
                        new HashMap<ImportInfoKey, String>() { {
                            put(ImportInfoKey.FIELD, "longitude");
                            put(ImportInfoKey.OLD_VALUE,
                                    persistBs.getLongitude() + "");
                            put(ImportInfoKey.NEW_VALUE,
                                    bs.getLongitude() + "");
                        } }
                    ));
                    persistBs.setLongitude(bs.getLongitude());
                }
                if (!updEvents.isEmpty()) {
                    update.add(persistBs);
                    events.addAll(updEvents);
                }
            }
        }
        LOG.info("create bus stops [" + create.size() + "]");
        LOG.info("update bus stops [" + update.size() + "]");
        if (isPersist) {
            dataService.createAll(create);
            dataService.updateAll(update);
        }
        return events;
    }
    /**
     * Create event.
     * @param <T> event entity type.
     * @param entity entity.
     * @param type event type.
     * @param info event details.
     * @return event.
     */
    private <T> ImportDataEvent createEvent(final T entity,
            final ImportDataEventType type,
            final Map<ImportInfoKey, String> info) {
        ImportDataEvent ev = new ImportDataEvent();
        ev.setTrigger(entity.toString());
        ev.setType(type);
        ev.setInfo(info);
        return ev;
    }
    /**
     * Create map with full entities set.
     * @param <T> external ref entity type.
     * @param cl entity class.
     * @param pid transport profile ID.
     * @return map, key - external ID, value - entity.
     * @throws Exception error.
     */
    private <T extends ExternalRef> Map<Long, T> createFullMap(
            final Class<T> cl, final Integer pid) throws Exception {
        List<T> all = transportService.getFromProfile(pid, cl);
        Map<Long, T> map = new HashMap<>();
        all.stream().forEach(p -> {
            if (p.getExternalId() != null) {
                map.put(p.getExternalId(), p);
            }
        });
        return map;
    }
}
