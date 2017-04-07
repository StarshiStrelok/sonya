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
 * Route type.
 * @author ss
 */
public enum RouteType {
    /** Autobus. */
    AUTOBUS("common.route.autobus"),
    /** Trolleybus. */
    TROLLEYBUS("common.route.trolleybus"),
    /** Routing taxi. */
    TAXI("common.route.taxi"),
    /** Tram. */
    TRAM("common.route.tram"),
    /** Metro. */
    METRO("common.route.metro");
    /** i18n key. */
    private final String i18nKey;
    /**
     * Constructor.
     * @param pI18nKey i18n key.
     */
    RouteType(final String pI18nKey) {
        i18nKey = pI18nKey;
    }
    /**
     * Get i18n key.
     * @return key.
     */
    public String getI18nKey() {
        return i18nKey;
    }
}
