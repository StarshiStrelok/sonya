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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
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
// =========================== SET & GET ======================================
    
// ============================================================================
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
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
        return "ss.sonya.entity.BusStop[ id=" + id + " ]";
    }
}
