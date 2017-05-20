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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * OSM map layer settings.
 * @author ss
 */
@Entity
@Table(name = "map_layer")
public class MapLayer implements Serializable {
    /** Default UID. */
    private static final long serialVersionUID = 1L;
// =============================== FIELDS =====================================
    /** Primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    /** Name. */
    @NotNull
    @Size(max = 50)
    @Column(name = "name", length = 50)
    private String name;
    /** Layer URL. */
    @NotNull
    @Size(max = 200)
    @Column(name = "url", length = 200)
    private String url;
    /** Layer material design icon. */
    @NotNull
    @Size(max = 200)
    @Column(name = "md_icon", length = 200)
    private String mdIcon;
    /** Transport profile. */
    @JsonIgnore
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transport_profile_id")
    private TransportProfile transportProfile;
// =============================== SET & GET ==================================
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
     * @return the url
     */
    public String getUrl() {
        return url;
    }
    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
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
     * @return the mdIcon
     */
    public String getMdIcon() {
        return mdIcon;
    }
    /**
     * @param mdIcon the mdIcon to set
     */
    public void setMdIcon(String mdIcon) {
        this.mdIcon = mdIcon;
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
        if (!(object instanceof MapLayer)) {
            return false;
        }
        MapLayer other = (MapLayer) object;
        return !((this.id == null && other.id != null)
                || (this.id != null && !this.id.equals(other.id)));
    }
    @Override
    public String toString() {
        return "ss.sonya.entity.MapLayer[ id=" + getId() + " ]";
    }
}
