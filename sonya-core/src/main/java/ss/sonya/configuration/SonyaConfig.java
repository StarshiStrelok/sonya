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
package ss.sonya.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;
import java.util.Set;
import org.apache.log4j.Logger;
import ss.sonya.constants.SonyaProperty;

/**
 * Read sonya.properties file from /conf folder.
 * @author ss
 */
public final class SonyaConfig {
    /** Logger. */
    private static final Logger LOG  = Logger.getLogger(SonyaConfig.class);
    /** Application properties. */
    private static final Properties PROPS = new Properties();
    /**
     * Initialization.
     */
    static {
        try {
            String file = "/sonya.properties";
            LOG.info("load " + file + " properties start...");
            File prFile = new File(System.getProperty("catalina.base")
                    + "/conf/" + file);
            try (InputStream is = new FileInputStream(prFile);
                Reader reader = new InputStreamReader(is, "UTF-8");) {
                PROPS.load(reader);
            }
            LOG.info("sonya.properties loaded...");
        } catch (IOException ex) {
            LOG.error("sonya.properties not loaded!", ex);
        }
    }
    /**
     * Private constructor.
     */
    private SonyaConfig() {
    }
    /**
     * Get property value as string.
     * @param ap - property key.
     * @return - property value or null.
     */
    public static String setting(final SonyaProperty ap) {
        return PROPS.getProperty(ap.getKey(), null);
    }
    /**
     * Get property value as string.
     * @param ap - property key.
     * @param def default value.
     * @return - property value or null.
     */
    public static String setting(final SonyaProperty ap, final String def) {
        return PROPS.getProperty(ap.getKey(), def);
    }
    /**
     * Get property value as integer.
     * @param ap - property key.
     * @return - property value.
     */
    public static int settingI(final SonyaProperty ap) {
        return Integer.valueOf(PROPS.getProperty(ap.getKey(), null));
    }
    /**
     * Get property value as integer.
     * @param ap - property key.
     * @param def - default value.
     * @return - property value.
     */
    public static int settingI(final SonyaProperty ap, final int def) {
        return Integer.valueOf(PROPS.getProperty(ap.getKey(), def + ""));
    }
    /**
     * Get property value as double.
     * @param ap - property key.
     * @return - property value.
     */
    public static double settingD(final SonyaProperty ap) {
        return Double.valueOf(PROPS.getProperty(ap.getKey(), null));
    }
    /**
     * Get property value as boolean.
     * @param ap - property key.
     * @return - property value.
     */
    public static Boolean settingB(final SonyaProperty ap) {
        return Boolean.parseBoolean(PROPS.getProperty(ap.getKey()));
    }
    /**
     * Get all keys.
     * @return - all keys.
     */
    public static Set<String> getKeys() {
        return PROPS.stringPropertyNames();
    }
}
