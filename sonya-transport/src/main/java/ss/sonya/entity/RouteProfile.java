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

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * Route profile.
 * @author ss
 */
@Entity
@Table(name = "route_profile")
public class RouteProfile implements Serializable {
    /** Default UID. */
    private static final long serialVersionUID = 1L;
// ============================= FIELDS =======================================
    /** Primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    /** Name. */
    @NotNull
    @Size(max = 30)
    @Column(name = "name", length = 30)
    private String name;
    /** Average speed. Km/h */
    @NotNull
    @Column(name = "avg_speed")
    private Double avgSpeed;
    /** Routing service URL. */
    @Pattern(regexp = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*"
            + "[-a-zA-Z0-9+&@#/%=~_|]")
    @Size(max = 100)
    @Column(name = "routing_url", length = 100)
    private String routingURL;
    /** Line color. */
    @Size(max = 10)
    @Column(name = "line_color", length = 10)
    private String lineColor;
    /** Last data update. */
    @Temporal(TemporalType.DATE)
    @Column(name = "last_update")
    private Date lastUpdate;
    /** Transport profile. */
    @JsonIgnore
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transport_profile_id")
    private TransportProfile transportProfile;
    /** Bus stop marker. */
    @JsonIgnore
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name="REPORT")
    private byte[] busStopMarker;
// ============================= SET & GET ====================================
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
     * @return the avgSpeed
     */
    public Double getAvgSpeed() {
        return avgSpeed;
    }
    /**
     * @param avgSpeed the avgSpeed to set
     */
    public void setAvgSpeed(Double avgSpeed) {
        this.avgSpeed = avgSpeed;
    }
    /**
     * @return the transportProfile
     */
    public TransportProfile getTransportProfile() {
        return transportProfile;
    }
    /**
     * @param transportProfile the transportProfile to set
     */
    public void setTransportProfile(TransportProfile transportProfile) {
        this.transportProfile = transportProfile;
    }
    /**
     * @return the routingURL
     */
    public String getRoutingURL() {
        return routingURL;
    }
    /**
     * @param routingURL the routingURL to set
     */
    public void setRoutingURL(String routingURL) {
        this.routingURL = routingURL;
    }
    /**
     * @return the lineColor
     */
    public String getLineColor() {
        return lineColor;
    }
    /**
     * @param lineColor the lineColor to set
     */
    public void setLineColor(String lineColor) {
        this.lineColor = lineColor;
    }
    /**
     * @return the lastUpdate
     */
    public Date getLastUpdate() {
        return lastUpdate;
    }
    /**
     * @param lastUpdate the lastUpdate to set
     */
    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
    /**
     * @return the busStopMarker
     */
    public byte[] getBusStopMarker() {
        return busStopMarker;
    }
    /**
     * @param busStopMarker the busStopMarker to set
     */
    public void setBusStopMarker(byte[] busStopMarker) {
        this.busStopMarker = busStopMarker;
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
        if (!(object instanceof RouteProfile)) {
            return false;
        }
        RouteProfile other = (RouteProfile) object;
        return !((this.id == null && other.id != null)
                || (this.id != null && !this.id.equals(other.id)));
    }
    @Override
    public String toString() {
        return "ss.sonya.entity.RouteProfile[ id=" + getId() + " ]";
    }
}
