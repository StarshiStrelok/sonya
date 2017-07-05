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
package ss.sonya.constants;

/**
 * Property in file sonya.properties.
 * @author ss
 */
public enum SonyaProperty {
    /** Database driver name. */
    DS_DRIVER("ds_driver"),
    /** Database URL. */
    DS_URL("ds_url"),
    /** Database user. */
    DS_USER("ds_user"),
    /** Database password. */
    DS_PASSWORD("ds_password"),
    /** Hibernate. Show SQL. */
    H_SHOW_SQL("hibernate.show_sql"),
    /** Hibernate. Dialect */
    H_DIALECT("hibernate.dialect"),
    /** Hibernate. Update schema mode. */
    H_HBM2DDL_AUTO("hibernate.hbm2ddl.auto"),
    /** Hibernate. Pool min size. */
    H_C3P0_MIN_SIZE("hibernate.c3p0.min_size"),
    /** Hibernate. Pool max size. */
    H_C3P0_MAX_SIZE("hibernate.c3p0.max_size"),
    /** Hibernate. Connection timeout.*/
    H_C3P0_TIMEOUT("hibernate.c3p0.timeout"),
    /** Hibernate. Pool max statements for caching. */
    H_C3P0_MAX_STATEMENTS("hibernate.c3p0.max_statements");
    /** Property key. */
    private final String key;
    /**
     * Constructor.
     * @param pKey - property key.
     */
    SonyaProperty(final String pKey) {
        key = pKey;
    }
    /**
     * Get property key.
     * @return - property key.
     */
    public String getKey() {
        return key;
    }
    /**
     * Get constant by key.
     * @param key - key.
     * @return - constant or null.
     */
    public static SonyaProperty getConstantByKey(final String key) {
        for (SonyaProperty ap : values()) {
            if (ap.key.equals(key)) {
                return ap;
            }
        }
        return null;
    }
}
