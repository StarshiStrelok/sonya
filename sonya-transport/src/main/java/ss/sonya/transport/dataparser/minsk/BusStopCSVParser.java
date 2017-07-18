/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ss.sonya.transport.dataparser.minsk;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ss.sonya.entity.BusStop;
import ss.sonya.transport.api.TransportDataService;

/**
 * Parse CSV data with bus stops info.
 * @author ss
 */
@Component
class BusStopCSVParser {
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(BusStopCSVParser.class);
    /** CSV resource. */
    private static final String FILE = "/ss/kira/data/minsk/busstops.csv";
    /** Transport service. */
    @Autowired
    private TransportDataService transportService;
    /**
     * Parse CSV.
     * @return bus stops.
     * @throws Exception parse error.
     */
    public List<BusStop> parse() throws Exception {
        LOG.info("######### start parsing...");
        List<BusStop> exist = transportService.getFromProfile(4, BusStop.class);
        Map<Long, BusStop> bsMap = new HashMap<>();
        for (BusStop b : exist) {
            bsMap.put(b.getExternalId(), b);
        }
        int fakeId = 0;
        List<BusStop> bsList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            getClass().getResourceAsStream(FILE)))) {
            String line;
            String lastName = "";
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] row = line.split(";");
                if ("ID".equals(row[0])) {
                    continue;
                }
                if (row[4].isEmpty()) {
                    row[4] = lastName;
                }
                if ("0".equals(row[6]) && "0".equals(row[7])) {
                    continue;
                }
                LOG.debug("bus stop [ id=" + row[0] + ", name="
                        + row[4] + ", lon=" + row[6] + ", lat="
                        + row[7] + " ]");
                if (!row[4].isEmpty()) {
                    BusStop bs = new BusStop();
                    bs.setExternalId(Long.valueOf(row[0]));
                    if (bsMap.containsKey(bs.getExternalId())) { // override
                        BusStop eBs = bsMap.get(bs.getExternalId());
                        bs.setLatitude(eBs.getLatitude());
                        bs.setLongitude(eBs.getLongitude());
                        bs.setName(eBs.getName());
                    } else {
                        bs.setLatitude(parseLatLon(row[7]));
                        bs.setLongitude(parseLatLon(row[6]));
                        bs.setName(row[4]);
                    }
                    bs.setId(++fakeId);
                    bsList.add(bs);
                    lastName = bs.getName();
                    if (bs.getName().isEmpty() || bs.getLatitude() <= 0
                            || bs.getLongitude() <= 0) {
                        throw new IllegalArgumentException("invalid line ["
                                + line + "]");
                    }
                } else {
                    throw new IllegalArgumentException("wrong line ["
                            + line + "]");
                }
            }
        }
        LOG.info("### bus stops size --> " + bsList.size());
        LOG.info("######### complete...");
        return bsList;
    }
    /**
     * Parse latitude/longitude.
     * @param str - coordinate string.
     * @return - double value.
     */
    private Double parseLatLon(String str) {
        str = str.substring(0, 2) + "." + str.substring(2);
        return Double.valueOf(str);
    }
}
