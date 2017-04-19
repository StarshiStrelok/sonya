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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Route.
 * @author ss
 */
@Entity
@Table(name = "routes")
public class Route implements Serializable {
    /** Default UID. */
    private static final long serialVersionUID = 1L;
// ============================ FIELDS ========================================
    /** Primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    /** TRoute type. */
    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "route_profile_id")
    private RouteProfile type;
    /** TRoute name prefix. */
    @NotNull
    @Size(max = 10)
    @Column(name = "name_prefix", length = 10)
    private String namePrefix;
    /** TRoute name postfix. */
    @Size(max = 10)
    @Column(name = "name_postfix", length = 10)
    private String namePostfix;
    /** TRoute paths. */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "route",
            cascade = CascadeType.ALL)
    private List<Path> paths;
    /** Alternative ID. */
    @Column(name = "external_id")
    private Long externalId;
    /** Transport profile. */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private TransportProfile transportProfile;
// ============================ SET & GET =====================================
    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }
    /**
     * @return the type
     */
    public RouteProfile getType() {
        return type;
    }
    /**
     * @param type the type to set
     */
    public void setType(RouteProfile type) {
        this.type = type;
    }
    /**
     * @return the namePrefix
     */
    public String getNamePrefix() {
        return namePrefix;
    }
    /**
     * @param namePrefix the namePrefix to set
     */
    public void setNamePrefix(String namePrefix) {
        this.namePrefix = namePrefix;
    }
    /**
     * @return the namePostfix
     */
    public String getNamePostfix() {
        return namePostfix;
    }
    /**
     * @param namePostfix the namePostfix to set
     */
    public void setNamePostfix(String namePostfix) {
        this.namePostfix = namePostfix;
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
        if (!(object instanceof Route)) {
            return false;
        }
        Route other = (Route) object;
        return !((this.id == null && other.id != null)
                || (this.id != null && !this.id.equals(other.id)));
    }
    @Override
    public String toString() {
        return "ss.sonya.entity.Route[ id=" + getId() + " ]";
    }
}
