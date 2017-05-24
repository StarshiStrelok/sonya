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
package ss.sonya.transport.search.vo;

import java.util.List;
import ss.sonya.entity.RouteProfile;

/**
 * Search settings.
 * @author ss
 */
public class SearchSettings {
    /** Start latitude. */
    private double startLat;
    /** Start longitude. */
    private double startLon;
    /** End latitude. */
    private double endLat;
    /** End longitude. */
    private double endLon;
    /** Transport profile ID. */
    private Integer profileId;
    /** Trip day. */
    private int day;
    /** Trip time. */
    private String time;
    /** Max result count. */
    private int maxResults;
    /** Max transfers. */
    private int maxTransfers;
    /** Disabled route types. */
    private List<RouteProfile> disabledRouteTypes;
    /**
     * @return the sLat
     */
    public double getStartLat() {
        return startLat;
    }
    /**
     * @param pStartLat the sLat to set
     */
    public void setStartLat(final double pStartLat) {
        startLat = pStartLat;
    }
    /**
     * @return the sLon
     */
    public double getStartLon() {
        return startLon;
    }
    /**
     * @param pStartLon the sLon to set
     */
    public void setStartLon(final double pStartLon) {
        startLon = pStartLon;
    }
    /**
     * @return the eLat
     */
    public double getEndLat() {
        return endLat;
    }
    /**
     * @param pEndLat the eLat to set
     */
    public void setEndLat(final double pEndLat) {
        endLat = pEndLat;
    }
    /**
     * @return the eLon
     */
    public double getEndLon() {
        return endLon;
    }
    /**
     * @param pEndLon the eLon to set
     */
    public void setEndLon(final double pEndLon) {
        endLon = pEndLon;
    }
    /**
     * @return the day
     */
    public int getDay() {
        return day;
    }
    /**
     * @param pDay the day to set
     */
    public void setDay(final int pDay) {
        day = pDay;
    }
    /**
     * @return the time
     */
    public String getTime() {
        return time;
    }
    /**
     * @param pTime the time to set
     */
    public void setTime(final String pTime) {
        time = pTime;
    }
    /**
     * @return the maxResults
     */
    public int getMaxResults() {
        return maxResults;
    }
    /**
     * @param pMaxResults the maxResults to set
     */
    public void setMaxResults(final int pMaxResults) {
        maxResults = pMaxResults;
    }
    /**
     * @return the maxTransfers
     */
    public int getMaxTransfers() {
        return maxTransfers;
    }
    /**
     * @param pMaxTransfers the maxTransfers to set
     */
    public void setMaxTransfers(final int pMaxTransfers) {
        maxTransfers = pMaxTransfers;
    }
    /**
     * @return the profileId
     */
    public Integer getProfileId() {
        return profileId;
    }
    /**
     * @param profileId the profileId to set
     */
    public void setProfileId(Integer profileId) {
        this.profileId = profileId;
    }
    /**
     * @return the disabledRouteTypes
     */
    public List<RouteProfile> getDisabledRouteTypes() {
        return disabledRouteTypes;
    }
    /**
     * @param disabledRouteTypes the disabledRouteTypes to set
     */
    public void setDisabledRouteTypes(List<RouteProfile> disabledRouteTypes) {
        this.disabledRouteTypes = disabledRouteTypes;
    }
}
