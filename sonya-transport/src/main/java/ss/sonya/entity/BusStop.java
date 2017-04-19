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
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Bus stop.
 * @author ss
 */
@Entity
@Table(name = "bus_stops")
public class BusStop implements Serializable {
    /** Default UID. */
    private static final long serialVersionUID = 1L;
// =========================== FIELDS =========================================
    /** Primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    /** Bus stop latitude. */
    @NotNull
    @Column(name = "latitude")
    private Double latitude;
    /** Bus stop longitude. */
    @NotNull
    @Column(name = "longitude")
    private Double longitude;
    /** Bus stop name. */
    @Size(min = 1, max = 100)
    @Column(length = 100)
    private String name;
    /** Paths. */
    @ManyToMany(mappedBy = "busstops", fetch = FetchType.LAZY)
    private List<Path> paths;
    /** External ID. */
    @Column(name = "external_id")
    private Long externalId;
    /** Transport profile. */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private TransportProfile transportProfile;
// =========================== SET & GET ======================================
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
     * @return the latitude
     */
    public Double getLatitude() {
        return latitude;
    }
    /**
     * @param latitude the latitude to set
     */
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    /**
     * @return the longitude
     */
    public Double getLongitude() {
        return longitude;
    }
    /**
     * @param longitude the longitude to set
     */
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
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
     * @return the paths
     */
    @JsonIgnore
    public List<Path> getPaths() {
        return paths;
    }
    /**
     * @param paths the paths to set
     */
    public void setPaths(List<Path> paths) {
        this.paths = paths;
    }
    /**
     * @return the externalId
     */
    public Long getExternalId() {
        return externalId;
    }
    /**
     * @param externalId the externalId to set
     */
    public void setExternalId(Long externalId) {
        this.externalId = externalId;
    }
    /**
     * @return the transportProfile
     */
    @JsonIgnore
    public TransportProfile getTransportProfile() {
        return transportProfile;
    }
    /**
     * @param transportProfile the transportProfile to set
     */
    public void setTransportProfile(TransportProfile transportProfile) {
        this.transportProfile = transportProfile;
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
        if (!(object instanceof BusStop)) {
            return false;
        }
        BusStop other = (BusStop) object;
        return !((this.id == null && other.id != null)
                || (this.id != null && !this.id.equals(other.id)));
    }
    @Override
    public String toString() {
        return "ss.sonya.entity.BusStop[ id=" + getId() + " ]";
    }
}
