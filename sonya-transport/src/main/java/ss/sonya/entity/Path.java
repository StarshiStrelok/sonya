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
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import ss.sonya.transport.iface.ExternalRef;

/**
 * Route path.
 * @author ss
 */
@Entity
@Table(name = "paths")
public class Path implements Serializable, ExternalRef {
    /** Default UID. */
    private static final long serialVersionUID = 1L;
// ====================== FIELDS ==============================================
    /** Primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    /** Route. */
    @ManyToOne(fetch = FetchType.EAGER)
    private Route route;
    /** Route paths. */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "path_busstops", joinColumns =
            @JoinColumn(name = "path_fk"), inverseJoinColumns =
            @JoinColumn(name = "busstop_fk"))
    @OrderColumn(name = "path_busstop_order")
    private List<BusStop> busstops;
    /** Path schedule. */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL,
            mappedBy = "path", orphanRemoval = true)
    private List<Trip> schedule;
    /** Path name. */
    @NotNull
    @Size(max = 100)
    @Column(name = "description", length = 100)
    private String description;
    /** Alternative ID, OSM for example. */
    @Column(name = "external_id")
    private Long externalId;
    /** Transport profile. */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transport_profile_id")
    private TransportProfile transportProfile;
// ====================== SET & GET ===========================================
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
     * @return the route
     */
    public Route getRoute() {
        return route;
    }
    /**
     * @param route the route to set
     */
    public void setRoute(Route route) {
        this.route = route;
    }
    /**
     * @return the busstops
     */
    public List<BusStop> getBusstops() {
        return busstops;
    }
    /**
     * @param busstops the busstops to set
     */
    public void setBusstops(List<BusStop> busstops) {
        this.busstops = busstops;
    }
    /**
     * @return the schedule
     */
    @JsonIgnore
    public List<Trip> getSchedule() {
        return schedule;
    }
    /**
     * @param schedule the schedule to set
     */
    public void setSchedule(List<Trip> schedule) {
        this.schedule = schedule;
    }
    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
    /**
     * @return the externalId
     */
    @Override
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
        if (!(object instanceof Path)) {
            return false;
        }
        Path other = (Path) object;
        return !((this.id == null && other.id != null)
                || (this.id != null && !this.id.equals(other.id)));
    }
    @Override
    public String toString() {
        String route = getRoute() == null ? "" : getRoute().getNamePrefix()
                + (getRoute().getNamePostfix() == null
                ? "" : getRoute().getNamePostfix()) + " "
                + getRoute().getType().getName();
        return "ss.sonya.entity.Path[ id=" + getId()
                + ", name=" + getDescription() + ", route=" + route + " ]";
    }
}
