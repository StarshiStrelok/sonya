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
package ss.sonya.entity;

import java.io.Serializable;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Transport profile, include locality & search settings.
 * @author ss
 */
@Entity
@Table(name = "transport_profile")
public class TransportProfile implements Serializable {
    /** Default UID. */
    private static final long serialVersionUID = 1L;
// ================================= FIELDS ===================================
    /** Primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    /** South-west latitude. */
    @NotNull
    @Max(90)
    @Min(-90)
    @Column(name = "south_west_lat")
    private Double southWestLat;
    /** South-west longitude. */
    @NotNull
    @Max(180)
    @Min(-180)
    @Column(name = "south_west_lon")
    private Double southWestLon;
    /** North-east latitude. */
    @NotNull
    @Max(90)
    @Min(-90)
    @Column(name = "north_east_lat")
    private Double northEastLat;
    /** North-east longitude. */
    @NotNull
    @Max(180)
    @Min(-180)
    @Column(name = "north_east_lon")
    private Double northEastLon;
    /** Map initial zoom. */
    @NotNull
    @Min(0)
    @Max(19)
    @Column(name = "initial_zoom")
    private Integer initialZoom;
    /** Minimal map zoom. */
    @NotNull
    @Min(0)
    @Max(19)
    @Column(name = "min_zoom")
    private Integer minZoom;
    /** Center latitude. */
    @NotNull
    @Max(90)
    @Min(-90)
    @Column(name = "center_lat")
    private Double centerLat;
    /** Center longitude. */
    @NotNull
    @Max(180)
    @Min(-180)
    @Column(name = "center_lon")
    private Double centerLon;
    /** Profile name. */
    @NotNull
    @Size(max = 100)
    @Column(name = "name", length = 100)
    private String name;
    /**
     * Bus stop access zone radius in km.
     * Used for define transfer bus stops for current bus stop.
     */
    @NotNull
    @Min(0)
    private Double busStopAccessZoneRadius;
    /**
     * Limits the number of bus stops to search near
     * the start and end points.
     */
    @NotNull
    @Min(1)
    private Integer searchLimitForPoints;
    /** Route profiles. */
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL,
            orphanRemoval = true, mappedBy = "transportProfile")
    private List<RouteProfile> routeProfiles;
// ================================= SET & GET ================================
    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }
    /**
     * @return the southWestLat
     */
    public Double getSouthWestLat() {
        return southWestLat;
    }
    /**
     * @param southWestLat the southWestLat to set
     */
    public void setSouthWestLat(Double southWestLat) {
        this.southWestLat = southWestLat;
    }
    /**
     * @return the southWestLon
     */
    public Double getSouthWestLon() {
        return southWestLon;
    }
    /**
     * @param southWestLon the southWestLon to set
     */
    public void setSouthWestLon(Double southWestLon) {
        this.southWestLon = southWestLon;
    }
    /**
     * @return the northEastLat
     */
    public Double getNorthEastLat() {
        return northEastLat;
    }
    /**
     * @param northEastLat the northEastLat to set
     */
    public void setNorthEastLat(Double northEastLat) {
        this.northEastLat = northEastLat;
    }
    /**
     * @return the northEastLon
     */
    public Double getNorthEastLon() {
        return northEastLon;
    }
    /**
     * @param northEastLon the northEastLon to set
     */
    public void setNorthEastLon(Double northEastLon) {
        this.northEastLon = northEastLon;
    }
    /**
     * @return the initialZoom
     */
    public Integer getInitialZoom() {
        return initialZoom;
    }
    /**
     * @param initialZoom the initialZoom to set
     */
    public void setInitialZoom(Integer initialZoom) {
        this.initialZoom = initialZoom;
    }
    /**
     * @return the minZoom
     */
    public Integer getMinZoom() {
        return minZoom;
    }
    /**
     * @param minZoom the minZoom to set
     */
    public void setMinZoom(Integer minZoom) {
        this.minZoom = minZoom;
    }
    /**
     * @return the centerLat
     */
    public Double getCenterLat() {
        return centerLat;
    }
    /**
     * @param centerLat the centerLat to set
     */
    public void setCenterLat(Double centerLat) {
        this.centerLat = centerLat;
    }
    /**
     * @return the centerLon
     */
    public Double getCenterLon() {
        return centerLon;
    }
    /**
     * @param centerLon the centerLon to set
     */
    public void setCenterLon(Double centerLon) {
        this.centerLon = centerLon;
    }
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * @return the routeProfiles
     */
    public List<RouteProfile> getRouteProfiles() {
        return routeProfiles;
    }
    /**
     * @param routeProfiles the routeProfiles to set
     */
    public void setRouteProfiles(List<RouteProfile> routeProfiles) {
        this.routeProfiles = routeProfiles;
    }
    /**
     * @return the busStopAccessZoneRadius
     */
    public Double getBusStopAccessZoneRadius() {
        return busStopAccessZoneRadius;
    }
    /**
     * @param busStopAccessZoneRadius the busStopAccessZoneRadius to set
     */
    public void setBusStopAccessZoneRadius(Double busStopAccessZoneRadius) {
        this.busStopAccessZoneRadius = busStopAccessZoneRadius;
    }
    /**
     * @return the searchLimitForPoints
     */
    public Integer getSearchLimitForPoints() {
        return searchLimitForPoints;
    }
    /**
     * @param searchLimitForPoints the searchLimitForPoints to set
     */
    public void setSearchLimitForPoints(Integer searchLimitForPoints) {
        this.searchLimitForPoints = searchLimitForPoints;
    }
// ============================================================================
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (getId() != null ? getId().hashCode() : 0);
        return hash;
    }
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof TransportProfile)) {
            return false;
        }
        TransportProfile other = (TransportProfile) object;
        return !((this.id == null && other.id != null)
                || (this.id != null && !this.id.equals(other.id)));
    }
    @Override
    public String toString() {
        return "ss.sonya.entity.TransportProfile[ id=" + getId() + ", name="
                + getName() + " ]";
    }
}
