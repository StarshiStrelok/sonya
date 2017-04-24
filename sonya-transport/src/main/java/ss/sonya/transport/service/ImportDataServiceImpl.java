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
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ss.sonya.entity.BusStop;
import ss.sonya.entity.Path;
import ss.sonya.entity.Route;
import ss.sonya.entity.RouteProfile;
import ss.sonya.entity.TransportProfile;
import ss.sonya.entity.Trip;
import ss.sonya.inject.DataService;
import ss.sonya.transport.api.ImportData;
import ss.sonya.transport.api.ImportDataSerializer;
import ss.sonya.transport.api.ImportDataService;
import ss.sonya.transport.component.ImportDataEvent;
import ss.sonya.transport.constants.ImportDataEventType;
import ss.sonya.transport.constants.ImportInfoKey;
import ss.sonya.transport.exception.EmptyFieldException;
import ss.sonya.transport.exception.ImportDataException;

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
                events.addAll(handleBusStops(busstops, isPersist));
            }
            // ---------------------- routes ----------------------------------
            List<Route> routes = data.routes();
            if (routes != null && !routes.isEmpty()) {
                handleRoutes(routes, sb);
            }
            // ---------------------- paths -----------------------------------
            List<Path> paths = data.paths();
            if (paths != null && !paths.isEmpty()) {
                handlePaths(paths, sb);
            }
            // ---------------------- schedule --------------------------------
            Map<Path, List<Trip>> sch = data.schedule();
            if (sch != null && !sch.isEmpty()) {
                handleSchedule(sch, sb);
            }
            // ---------------------- orphan routes & paths -------------------
            deleteOrphanRoutesAndPaths(paths, routes, sb);
            // ---------------------- orphan bus stops ------------------------
            deleteOrphanBusStops(sb);
            return events;
        } catch (Exception e) {
            LOG.error("import data error!", e);
            throw new ImportDataException("import data error!", e);
        }
    }
// ================================== PRIVATE =================================
    /**
     * Delete orphan bus stops.
     * @param sb log container.
     * @throws Exception method error.
     */
    private void deleteOrphanBusStops(final StringBuilder sb) throws Exception {
        int deleted = 0;
        for (BusStop bs : dataService.getAll(BusStop.class)) {
            if (bsDAO.findBusStopPaths(bs.getId()).isEmpty()) {
                bsDAO.delete(bs.getId());
                deleted++;
                String msg = "^^ delete orphan bus stop: " + bs;
                LOG.info(msg);
                sb.append(msg).append(BR);
            }
        }
        String msg = "^^ orphan bus stops size [" + deleted + "]";
        LOG.info(msg);
        sb.append(msg).append(BR);
    }
    /**
     * Delete orphan paths & routes.
     * @param paths actual paths.
     * @param routes actual routes.
     * @param sb log container.
     * @throws Exception method error.
     */
    private void deleteOrphanRoutesAndPaths(final List<Path> paths,
            final List<Route> routes, final StringBuilder sb)
            throws Exception {
        List<Route> allRoutes = dataService.getAll(Route.class);
        Map<Long, Route> routeMap = new HashMap<>();
        Map<Long, Path> pathMap = new HashMap<>();
        for (Route r : allRoutes) {
            if (r.getExternalId() != null) {
                routeMap.put(r.getExternalId(), r);
            }
        }
        for (Route route : routes) {
            Route exist = routeMap.get(route.getExternalId());
            routeMap.remove(route.getExternalId());
            List<Path> existPaths = pathDAO.findRoutePaths(exist.getId());
            for (Path p : existPaths) {
                if (p.getExternalId() != null) {
                    pathMap.put(p.getExternalId(), p);
                }
            }
        }
        for (Path path : paths) {
            pathMap.remove(path.getExternalId());
        }
        String msg = "^^ orphan paths size [" + pathMap.size() + "]";
        LOG.info(msg);
        sb.append(msg).append(BR);
        msg = "^^ orphan routes size [" + routeMap.size() + "]";
        LOG.info(msg);
        sb.append(msg).append(BR);
        for (Path del : pathMap.values()) {
            msg = "^^ delete orphan path " + del;
            LOG.info(msg);
            sb.append(msg).append(BR);
            pathDAO.delete(del.getId());
        }
        for (Route del : routeMap.values()) {
            msg = "^^ delete orphan route " + del;
            LOG.info(msg);
            sb.append(msg).append(BR);
            routeDAO.delete(del.getId());
        }
    }
    /**
     * Handle schedule.
     * @param sch schedule.
     * @param sb log container.
     * @throws Exception method error.
     */
    private void handleSchedule(final Map<Path, List<Trip>> sch,
            final StringBuilder sb) throws Exception {
        String msg1 = "^^ schedule size [" + sch.size() + "]";
        LOG.info(msg1);
        sb.append(msg1).append(BR);
        int updatedSchedule = 0;
        for (Path p : sch.keySet()) {
            if (p.getExternalId() == null) {
                throw new EmptyFieldException("altId", p);
            }
            Path path = pathDAO.findByAltId(p.getExternalId());
            List<Trip> persist = pathDAO.getSchedule(path.getId());
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
                path.setSchedule(schedule);
                pathDAO.update(path);
                for (Trip t : persist) {
                    pathDAO.deleteTrip(t.getId());
                }
                updatedSchedule++;
                String msg = "^ schedule updated for: " + path;
                LOG.info(msg);
                sb.append(msg).append(BR);
            }
        }
        String msg2 = "^^ updated schedule [" + updatedSchedule + "]";
        LOG.info(msg2);
        sb.append(msg2).append(BR);
    }
    /**
     * Handle paths.
     * @param paths paths.
     * @param sb log container.
     * @throws Exception method error.
     */
    private void handlePaths(final List<Path> paths, final StringBuilder sb)
            throws Exception {
        String msg1 = "^^ paths found [" + paths.size() + "]";
        LOG.info(msg1);
        sb.append(msg1).append(BR);
        int newPaths = 0;
        int updatedPaths = 0;
        List<BusStop> all = bsDAO.getAll();
        Map<Long, BusStop> bsMap = new HashMap<>();
        for (BusStop bs : all) {
            bsMap.put(bs.getExternalId(), bs);
        }
        for (Path path : paths) {
            if (path.getExternalId() == null) {
                throw new EmptyFieldException("altId", path);
            }
            if (path.getRoute() == null || path.getRoute().getExternalId() == null) {
                throw new EmptyFieldException("route", path);
            }
            if (path.getBusStops() == null || path.getBusStops().isEmpty()) {
                throw new EmptyFieldException("busstops", path);
            }
            Route route = routeDAO.findByAltId(path.getRoute().getExternalId());
            Path persist = pathDAO.findByAltId(path.getExternalId());
            List<BusStop> pathBusStops = new ArrayList<>();
            for (BusStop bs : path.getBusStops()) {
                if (bs.getExternalId() == null) {
                    throw new EmptyFieldException("altId", bs);
                }
                if (!bsMap.containsKey(bs.getExternalId())) {
                    throw new IllegalArgumentException("bus stop with "
                            + "alternative ID [" + bs.getExternalId()
                            + "] not found!");
                }
                pathBusStops.add(bsMap.get(bs.getExternalId()));
            }
            if (persist == null) {
                newPaths++;
                path.setRoute(route);
                path.setBusStops(pathBusStops);
                pathDAO.create(path);
                String msg = "^ new path created: " + path;
                sb.append(msg).append(BR);
                LOG.info(msg);
            } else {
                boolean hasChanges = false;
                if (!persist.getDescription().equals(path.getDescription())) {
                    hasChanges = true;
                    String msg = "^ path old description ["
                            + persist.getDescription() + "], new description ["
                            + path.getDescription() + "]";
                    persist.setDescription(path.getDescription());
                    sb.append(msg).append(BR);
                    LOG.info(msg);
                }
                Route persistRoute = pathDAO.findPathRoute(persist.getId());
                if (!persistRoute.equals(route)) {
                    hasChanges = true;
                    String msg = "^ path old route ["
                            + persistRoute + "], new route [" + route + "]";
                    persist.setRoute(route);
                    sb.append(msg).append(BR);
                    LOG.info(msg);
                }
                List<BusStop> persistBusStops = bsDAO
                        .findPathBusStops(persist.getId());
                List<BusStop> mocks = new ArrayList<>();
                for (BusStop bs : persistBusStops) {
                    if (TransportConst.MOCK_BS.equals(bs.getName())) {
                        mocks.add(bs);
                    }
                }
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
                    persistBusStops = bsDAO.findPathBusStops(persist.getId());
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
                    hasChanges = true;
                    String msg = "^ path way changed";
                    persist.setBusStops(pathBusStops);
                    sb.append(msg).append("<b style=\"color:red;\">")
                            .append("Check mock bus stops</b>").append(BR);
                    LOG.info(msg);
                }
                if (hasChanges) {
                    pathDAO.update(persist);
                    updatedPaths++;
                    String msg = "^ path updated: " + persist;
                    LOG.info(msg);
                    sb.append(msg).append(BR);
                }
            }
        }
        String msg2 = "^^ created paths [" + newPaths
                + "], updated paths [" + updatedPaths + "]";
        LOG.info(msg2);
        sb.append(msg2).append(BR);
    }
    /**
     * Handle routes.
     * @param routes routes.
     * @param sb log container.
     * @throws Exception method error.
     */
    private void handleRoutes(final List<Route> routes, final StringBuilder sb)
            throws Exception {
        String msg1 = "^^ routes found [" + routes.size() + "]";
        LOG.info(msg1);
        sb.append(msg1).append(BR);
        int newRoutes = 0;
        int updatedRoutes = 0;
        for (Route route : routes) {
            if (route.getExternalId() == null) {
                throw new EmptyFieldException("altId", route);
            }
            Route persist = routeDAO.findByAltId(route.getExternalId());
            if (persist == null) {
                newRoutes++;
                routeDAO.create(route);
                String msg = "^ new route created: " + route;
                sb.append(msg).append(BR);
                LOG.info(msg);
            } else {
                boolean hasChanges = false;
                if (!persist.getNamePrefix().equals(route.getNamePrefix())) {
                    hasChanges = true;
                    String msg = "^ route old name prefix ["
                            + persist.getNamePrefix() + "], new name prefix ["
                            + route.getNamePrefix() + "]";
                    persist.setNamePrefix(route.getNamePrefix());
                    sb.append(msg).append(BR);
                    LOG.info(msg);
                }
                String persistNP = persist.getNamePostfix();
                persistNP = persistNP == null ? "" : persistNP;
                String routeNP = route.getNamePostfix();
                routeNP = routeNP == null ? "" : routeNP;
                if (!persistNP.equals(routeNP)) {
                    hasChanges = true;
                    String msg = "^ route old name postfix ["
                            + persist.getNamePostfix() + "], new name postfix ["
                            + route.getNamePostfix() + "]";
                    persist.setNamePostfix(route.getNamePostfix());
                    sb.append(msg).append(BR);
                    LOG.info(msg);
                }
                if (!persist.getType().equals(route.getType())) {
                    hasChanges = true;
                    String msg = "^ route old type ["
                            + persist.getType()
                            + "], new type [" + route.getType() + "]";
                    persist.setType(route.getType());
                    sb.append(msg).append(BR);
                    LOG.info(msg);
                }
                if (hasChanges) {
                    routeDAO.update(persist);
                    updatedRoutes++;
                    String msg = "^ route updated: " + persist;
                    LOG.info(msg);
                    sb.append(msg).append(BR);
                }
            }
        }
        String msg2 = "^^ created routes [" + newRoutes
                + "], updated routes [" + updatedRoutes + "]";
        LOG.info(msg2);
        sb.append(msg2).append(BR);
    }
    /**
     * Handle bus stops data.
     * @param busstops bus stops list.
     * @param sb log container.
     * @throws Exception method error.
     */
    private List<ImportDataEvent> handleBusStops(final List<BusStop> busstops,
            final boolean isPersist)
            throws Exception {
        List<ImportDataEvent> events = new ArrayList<>();
        List<BusStop> create = new ArrayList<>();
        List<BusStop> update = new ArrayList<>();
        List<BusStop> all = dataService.getAll(BusStop.class);
        Map<Long, BusStop> allMap = new HashMap<>();
        all.stream().forEach(bs -> {
            if (bs.getExternalId() != null) {
                allMap.put(bs.getExternalId(), bs);
            }
        });
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
                        new HashMap<ImportInfoKey, String>() {{
                            put(ImportInfoKey.FIELD, "name");
                            put(ImportInfoKey.OLD_VALUE, persistBs.getName());
                            put(ImportInfoKey.NEW_VALUE, bs.getName());
                        }}
                    ));
                    persistBs.setName(bs.getName());
                }
                if (!persistBs.getLatitude().equals(bs.getLatitude())) {
                    updEvents.add(createEvent(persistBs,
                            ImportDataEventType.BUS_STOP_UPDATE,
                        new HashMap<ImportInfoKey, String>() {{
                            put(ImportInfoKey.FIELD, "latitude");
                            put(ImportInfoKey.OLD_VALUE, persistBs.getLatitude() + "");
                            put(ImportInfoKey.NEW_VALUE, bs.getLatitude() + "");
                        }}
                    ));
                    persistBs.setLatitude(bs.getLatitude());
                }
                if (!persistBs.getLongitude().equals(bs.getLongitude())) {
                    updEvents.add(createEvent(persistBs,
                            ImportDataEventType.BUS_STOP_UPDATE,
                        new HashMap<ImportInfoKey, String>() {{
                            put(ImportInfoKey.FIELD, "longitude");
                            put(ImportInfoKey.OLD_VALUE, persistBs.getLongitude() + "");
                            put(ImportInfoKey.NEW_VALUE, bs.getLongitude() + "");
                        }}
                    ));
                    persistBs.setLongitude(bs.getLongitude());
                }
                if (!updEvents.isEmpty()) {
                    update.add(persistBs);
                    events.addAll(updEvents);
                }
            }
        }
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
        ev.setTrigger(entity);
        ev.setType(type);
        ev.setInfo(info);
        return ev;
    }
}
