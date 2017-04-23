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
package ss.sonya.transport.component;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import ss.sonya.entity.BusStop;
import ss.sonya.entity.Path;
import ss.sonya.entity.Route;
import ss.sonya.entity.Trip;
import ss.sonya.transport.api.ImportData;
import ss.sonya.transport.api.ImportDataSerializer;
import ss.sonya.transport.exception.DataDeserializationException;
import ss.sonya.transport.exception.DataSerializationException;
import ss.sonya.transport.exception.EmptyFieldException;

/**
 * Data serializer / deserializer.
 * @author ss
 */
@Component
public class JsonDataSerializer implements ImportDataSerializer {
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(
            JsonDataSerializer.class);
// ========================== JSON FIELDS =====================================
    /** -> Meta data. */
    private static final String META = "meta";
    /** --> Created. */
    private static final String CREATED = "created";
    /** -> Part 'bus stops'. */
    private static final String PART_BUSSTOPS = "part_busstops";
    /** -> Part 'routes'. */
    private static final String PART_ROUTES = "part_routes";
    /** -> Part 'paths'. */
    private static final String PART_PATHS = "part_paths";
    /** -> Part 'schedule'. */
    private static final String PART_SCHEDULE = "part_schedule";
    /** --> Alternative ID. */
    private static final String BS_EXT_ID = "external_id";
    /** --> Bus stop name. */
    private static final String BS_NAME = "name";
    /** --> Latitude. */
    private static final String BS_LAT = "lat";
    /** --> Longitude. */
    private static final String BS_LON = "lon";
    /** --> Route type. */
    private static final String ROUTE_TYPE = "type";
    /** --> Route name prefix. */
    private static final String ROUTE_N_PREFIX = "name_prefix";
    /** --> Route name postfix. */
    private static final String ROUTE_N_POSTFIX = "name_postfix";
    /** --> Route alternative ID. */
    private static final String ROUTE_EXT_ID = "external_id";
    /** --> Path alternative ID. */
    private static final String PATH_EXT_ID = "external_id";
    /** --> Path name. */
    private static final String PATH_NAME = "name";
    /** --> Path route. */
    private static final String PATH_ROUTE = "route";
    /** --> Path bus stops. */
    private static final String PATH_WAY = "way";
    /** --> Schedule path ID. */
    private static final String SCH_PATH_ID = "path_id";
    /** --> Schedule data. */
    private static final String SCH_DATA = "data";
    /** ---> Schedule days. */
    private static final String SCH_DAYS = "days";
    /** ---> Regular schedule. */
    private static final String SCH_REGULAR = "regular";
    /** ---> Irregular schedule. */
    private static final String SCH_IRREGULAR = "irregular";
// ============================================================================
    @Override
    public byte[] serialize(final ImportData data)
            throws DataSerializationException {
        try {
            LOG.info("&&& serializer start...");
            long start = System.currentTimeMillis();
            JsonObjectBuilder root = Json.createObjectBuilder();
            // ----------------- bus stops -------------------------------------
            if (data.busstops() != null && !data.busstops().isEmpty()) {
                LOG.info("& bus stops found");
                root.add(PART_BUSSTOPS, serBusstops(data.busstops()));
            }
            //------------------ routes ----------------------------------------
            if (data.routes() != null && !data.routes().isEmpty()) {
                LOG.info("& routes found");
                root.add(PART_ROUTES, serRoutes(data.routes()));
            }
            // ----------------- paths -----------------------------------------
            if (data.paths() != null && !data.paths().isEmpty()) {
                LOG.info("& paths found");
                root.add(PART_PATHS, serPaths(data.paths()));
            }
            // ----------------- schedule --------------------------------------
            if (data.schedule() != null && !data.schedule().isEmpty()) {
                LOG.info("& schedule found");
                root.add(PART_SCHEDULE, serSchedule(data.schedule()));
            }
            // ----------------- meta ------------------------------------------
            JsonObjectBuilder meta = Json.createObjectBuilder();
            meta.add(CREATED, new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
                    .format(new Date()));
            root.add(META, meta);
            LOG.info("& elapsed time [" + (System.currentTimeMillis() - start)
                    + "] ms");
            LOG.info("&&& serializer end...");
            return root.build().toString().getBytes("UTF-8");
        } catch (ConstraintViolationException | UnsupportedEncodingException
                | EmptyFieldException e) {
            LOG.error("serialize import data error!", e);
            throw new DataSerializationException(
                    "serialize import data error!", e);
        }
    }
    @Override
    public ImportData deserialize(final byte[] binData)
            throws DataDeserializationException {
        try {
            LOG.info("&&& deserializer start...");
            long start = System.currentTimeMillis();
            List<BusStop> busstops = null;
            List<Path> paths = null;
            List<Route> routes = null;
            Map<Path, List<Trip>> schedule = null;
            JSONObject root = new JSONObject(new String(binData, "UTF-8"));
            // ------------------------- bus stops ----------------------------
            if (root.has(PART_BUSSTOPS)) {
                busstops = deserBusstops(root.getJSONArray(PART_BUSSTOPS));
            }
            // ------------------------- routes -------------------------------
            if (root.has(PART_ROUTES)) {
                routes = deserRoutes(root.getJSONArray(PART_ROUTES));
            }
            // ------------------------- paths --------------------------------
            if (root.has(PART_PATHS)) {
                paths = deserPaths(root.getJSONArray(PART_PATHS));
            }
            // ------------------------- schedule -----------------------------
            if (root.has(PART_SCHEDULE)) {
                schedule = deserSchedule(root.getJSONArray(PART_SCHEDULE));
            }
            ImportData importData = new JsonImportData(
                    busstops, paths, routes, schedule);
            LOG.info("& elapsed time [" + (System.currentTimeMillis() - start)
                    + "] ms");
            LOG.info("&&& deserializer end...");
            return importData;
        } catch (UnsupportedEncodingException | EmptyFieldException e) {
            LOG.error("deserialize import data error!", e);
            throw new DataDeserializationException(
                    "deserialize import data error!", e);
        }
    }
// ============================= PRIVATE ======================================
    /**
     * Create bus stops JSON array.
     * @param bsList list bus stops.
     * @return JSON array.
     * @throws EmptyFieldException - field required.
     */
    private JsonArrayBuilder serBusstops(final List<BusStop> bsList)
            throws EmptyFieldException {
        JsonArrayBuilder arr = Json.createArrayBuilder();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        for (BusStop bs : bsList) {
            LOG.debug(bs);
            Set<ConstraintViolation<BusStop>> v = validator.validate(bs);
            if (!v.isEmpty()) {
                throw new ConstraintViolationException(v);
            }
            JsonObjectBuilder o = Json.createObjectBuilder();
            if (bs.getExternalId() == null) {
                throw new EmptyFieldException(BS_EXT_ID, bs);
            }
            o.add(BS_EXT_ID, bs.getExternalId());
            o.add(BS_NAME, bs.getName());
            o.add(BS_LAT, bs.getLatitude());
            o.add(BS_LON, bs.getLongitude());
            arr.add(o);
        }
        LOG.info("& added bus stops [" + bsList.size() + "]");
        return arr;
    }
    /**
     * Deserialize bus stops.
     * @param data JSON array.
     * @return list bus stops.
     */
    private List<BusStop> deserBusstops(final JSONArray data) {
        List<BusStop> list = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            JSONObject o = data.getJSONObject(i);
            BusStop bs = new BusStop();
            bs.setExternalId(o.getLong(BS_EXT_ID));
            bs.setName(o.getString(BS_NAME));
            bs.setLatitude(o.getDouble(BS_LAT));
            bs.setLongitude(o.getDouble(BS_LON));
            list.add(bs);
        }
        LOG.info("& restore bus stops [" + list.size() + "]");
        return list;
    }
    /**
     * Create routes JSON array.
     * @param routes routes list.
     * @return JSON array.
     * @throws EmptyFieldException field required.
     */
    private JsonArrayBuilder serRoutes(final List<Route> routes)
            throws EmptyFieldException {
        JsonArrayBuilder arr = Json.createArrayBuilder();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        for (Route route : routes) {
            LOG.debug(route);
            Set<ConstraintViolation<Route>> v = validator.validate(route);
            if (!v.isEmpty()) {
                throw new ConstraintViolationException(v);
            }
            JsonObjectBuilder o = Json.createObjectBuilder();
            if (route.getExternalId() == null) {
                throw new EmptyFieldException("altId", route);
            }
            o.add(ROUTE_EXT_ID, route.getExternalId());
            o.add(ROUTE_TYPE, route.getType().name());
            o.add(ROUTE_N_PREFIX, route.getNamePrefix());
            if (route.getNamePostfix() != null) {
                o.add(ROUTE_N_POSTFIX, route.getNamePostfix());
            }
            arr.add(o);
        }
        LOG.info("& added routes [" + routes.size() + "]");
        return arr;
    }
    /**
     * Deserialize routes.
     * @param data JSON array.
     * @return routes.
     */
    private List<Route> deserRoutes(final JSONArray data) {
        List<Route> list = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            JSONObject o = data.getJSONObject(i);
            Route route = new Route();
            route.setExternalId(o.getLong(ROUTE_EXT_ID));
            route.setNamePrefix(o.getString(ROUTE_N_PREFIX));
            if (o.has(ROUTE_N_POSTFIX)) {
                route.setNamePostfix(o.getString(ROUTE_N_POSTFIX));
            }
            route.setType(RouteType.valueOf(o.getString(ROUTE_TYPE)));
            list.add(route);
        }
        LOG.info("& restore routes [" + list.size() + "]");
        return list;
    }
    /**
     * Create paths JSON array.
     * @param paths paths.
     * @return JSON array.
     * @throws EmptyFieldException - field required.
     */
    private JsonArrayBuilder serPaths(final List<Path> paths)
            throws EmptyFieldException {
        JsonArrayBuilder arr = Json.createArrayBuilder();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        for (Path path : paths) {
            LOG.debug(path);
            Set<ConstraintViolation<Path>> v = validator.validate(path);
            if (!v.isEmpty()) {
                throw new ConstraintViolationException(v);
            }
            JsonObjectBuilder o = Json.createObjectBuilder();
            if (path.getExternalId() == null) {
                throw new EmptyFieldException("altId", path);
            }
            o.add(PATH_EXT_ID, path.getExternalId());
            o.add(PATH_NAME, path.getDescription());
            if (path.getRoute() == null || path.getRoute().getExternalId() == null) {
                throw new EmptyFieldException("route", path);
            }
            if (path.getRoute() == null) {
                throw new EmptyFieldException("route", path);
            }
            o.add(PATH_ROUTE, path.getRoute().getExternalId());
            if (path.getBusstops() == null || path.getBusstops().isEmpty()) {
                throw new EmptyFieldException("busstops", path);
            }
            JsonArrayBuilder way = Json.createArrayBuilder();
            for (BusStop bs : path.getBusstops()) {
                if (bs.getExternalId() == null) {
                    throw new EmptyFieldException("altId", bs);
                }
                way.add(bs.getExternalId());
            }
            o.add(PATH_WAY, way);
            arr.add(o);
        }
        LOG.info("& added paths [" + paths.size() + "]");
        return arr;
    }
    /**
     * Deserialize paths.
     * @param data JSON array.
     * @return paths.
     */
    private List<Path> deserPaths(final JSONArray data) {
        List<Path> list = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            JSONObject o = data.getJSONObject(i);
            Path path = new Path();
            path.setExternalId(o.getLong(PATH_EXT_ID));
            path.setDescription(o.getString(PATH_NAME));
            Route route = new Route();
            route.setExternalId(o.getLong(PATH_ROUTE));
            path.setRoute(route);
            JSONArray way = o.getJSONArray(PATH_WAY);
            List<BusStop> bsList = new ArrayList<>();
            for (int j = 0; j < way.length(); j++) {
                BusStop bs = new BusStop();
                bs.setExternalId(way.getLong(j));
                bsList.add(bs);
            }
            path.setBusstops(bsList);
            list.add(path);
        }
        LOG.info("& restore paths [" + list.size() + "]");
        return list;
    }
    /**
     * Create schedule JSON array.
     * @param schedule schedule.
     * @return JSON array.
     * @throws EmptyFieldException required field.
     */
    private JsonArrayBuilder serSchedule(
            final Map<Path, List<Trip>> schedule) throws EmptyFieldException {
        JsonArrayBuilder arr = Json.createArrayBuilder();
        for (Path p : schedule.keySet()) {
            JsonObjectBuilder oo = Json.createObjectBuilder();
            if (p.getExternalId() == null) {
                throw new EmptyFieldException("altId", p);
            }
            oo.add(SCH_PATH_ID, p.getExternalId());
            JsonArrayBuilder arr2 = Json.createArrayBuilder();
            for (Trip t : schedule.get(p)) {
                JsonObjectBuilder ooo = Json.createObjectBuilder();
                ooo.add(SCH_DAYS, t.getDays());
                if (t.getRegular() != null && !t.getRegular().isEmpty()) {
                    ooo.add(SCH_REGULAR, t.getRegular());
                }
                if (t.getIrregular() != null && !t.getIrregular().isEmpty()) {
                    ooo.add(SCH_IRREGULAR, t.getIrregular());
                }
                arr2.add(ooo);
            }
            oo.add(SCH_DATA, arr2);
            arr.add(oo);
        }
        LOG.info("& added schedule for [" + schedule.size() + "] paths");
        return arr;
    }
    /**
     * Deserialize schedule.
     * @param data JSON array.
     * @return schedule.
     * @throws EmptyFieldException - field required.
     */
    private Map<Path, List<Trip>> deserSchedule(final JSONArray data)
            throws EmptyFieldException {
        Map<Path, List<Trip>> schedule = new HashMap<>();
        JSONObject oo;
        JSONObject ooo;
        int fakeId = 0;
        for (int i = 0; i < data.length(); i++) {
            oo = data.getJSONObject(i);
            Path path = new Path();
            path.setId(++fakeId);
            path.setExternalId(oo.getLong(SCH_PATH_ID));
            List<Trip> list = new ArrayList<>();
            JSONArray arr2 = oo.getJSONArray(SCH_DATA);
            for (int j = 0; j < arr2.length(); j++) {
                ooo = arr2.getJSONObject(j);
                Trip t = new Trip();
                t.setDays(ooo.getString(SCH_DAYS));
                if (ooo.has(SCH_REGULAR)) {
                    t.setRegular(ooo.getString(SCH_REGULAR));
                } else if (ooo.has(SCH_IRREGULAR)) {
                    t.setIrregular(ooo.getString(SCH_IRREGULAR));
                } else {
                    throw new EmptyFieldException("regular | irregular", t);
                }
                if (t.getRegular() == null) {
                    t.setRegular("");
                }
                list.add(t);
            }
            schedule.put(path, list);
        }
        LOG.info("& restore schedule size [" + schedule.size() + "]");
        return schedule;
    }
}
