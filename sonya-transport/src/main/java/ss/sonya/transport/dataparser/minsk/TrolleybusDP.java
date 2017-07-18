/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ss.sonya.transport.dataparser.minsk;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ss.sonya.transport.dataparser.DataParser;
import ss.sonya.transport.dataparser.RouteType;
import ss.sonya.transport.iface.ImportData;

/**
 * Minsk: Trolleybus.
 * @author ss
 */
@Component("MinskTrolleybus")
class TrolleybusDP implements DataParser {
    /** Common parser. */
    @Autowired
    private JsonImportDataProducer parser;
    @Override
    public String name() {
        return "Minsk: Trolleybus";
    }
    @Override
    public ImportData parse() throws Exception {
        return parser.createData(RouteType.trol);
    }
}
