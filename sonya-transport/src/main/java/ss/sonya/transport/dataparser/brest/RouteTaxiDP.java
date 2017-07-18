/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ss.sonya.transport.dataparser.brest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ss.sonya.transport.dataparser.DataParser;
import ss.sonya.transport.dataparser.RouteType;
import ss.sonya.transport.iface.ImportData;

/**
 * Brest: route taxi data parser.
 * @author ss
 */
@Component("BrestRouteTaxi")
class RouteTaxiDP implements DataParser {
    /** All schedule parser. */
    @Autowired
    private AllScheduleParser parser;
    @Override
    public String name() {
        return "Brest: Route taxi";
    }
    @Override
    public ImportData parse() throws Exception {
        return parser.parse(RouteType.taxi);
    }
}
