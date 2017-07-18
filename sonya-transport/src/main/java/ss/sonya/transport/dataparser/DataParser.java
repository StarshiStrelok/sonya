/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ss.sonya.transport.dataparser;

import ss.sonya.transport.iface.ImportData;

/**
 * Data parser interface.
 * @author ss
 */
public interface DataParser {
    /**
     * Get data parser name.
     * @return 
     */
    String name();
    /**
     * Parse data.
     * @return formatted data.
     * @throws Exception error.
     */
    ImportData parse() throws Exception;
}
