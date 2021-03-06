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
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * Route profile marker image.
 * @author ss
 */
@Entity
@Table(name = "route_profile_marker_image")
public class RouteProfileMarkerImage implements Serializable {
    /** Default UID. */
    private static final long serialVersionUID = 1L;
// ========================== FIELDS ==========================================
    /** Primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    /** Bus stop marker. */
    @JsonIgnore
    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(name = "image_data")
    private byte[] data;
    /** Route profile. */
    @JsonIgnore
    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_profile_id")
    private RouteProfile profile;
// ========================== SET & GET =======================================
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
     * @return the image
     */
    public byte[] getData() {
        return data;
    }
    /**
     * @param data the image to set
     */
    public void setData(byte[] data) {
        this.data = data;
    }
    /**
     * @return the profile
     */
    public RouteProfile getProfile() {
        return profile;
    }
    /**
     * @param profile the profile to set
     */
    public void setProfile(RouteProfile profile) {
        this.profile = profile;
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
        if (!(object instanceof RouteProfileMarkerImage)) {
            return false;
        }
        RouteProfileMarkerImage other = (RouteProfileMarkerImage) object;
        return !((this.id == null && other.id != null)
                || (this.id != null && !this.id.equals(other.id)));
    }
    @Override
    public String toString() {
        return "ss.sonya.entity.RouteProfileMarkerImage[ id=" + getId() + " ]";
    }
}
