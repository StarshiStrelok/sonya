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
 * One trip.
 * @author ss
 */
@Entity
@Table(name = "trips")
public class Trip implements Serializable {
    /** Default UID. */
    private static final long serialVersionUID = 1L;
// ============================ FIELDS ========================================
    /** Primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    /** Regular schedule data. */
    @NotNull
    @Column(name = "regular", length = 3000)
    private String regular;
    /** Irregular schedule data. */
    @Size(max = 3000)
    @Column(name = "irregular", length = 3000)
    private String irregular;
    /** Trip days. */
    @NotNull
    @Column(name = "days")
    private String days;
    /** Path. */
    @JsonIgnore
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "path_id")
    private Path path;
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
     * @return the regular
     */
    public String getRegular() {
        return regular;
    }
    /**
     * @param regular the regular to set
     */
    public void setRegular(String regular) {
        this.regular = regular;
    }
    /**
     * @return the irregular
     */
    public String getIrregular() {
        return irregular;
    }
    /**
     * @param irregular the irregular to set
     */
    public void setIrregular(String irregular) {
        this.irregular = irregular;
    }
    /**
     * @return the days
     */
    public String getDays() {
        return days;
    }
    /**
     * @param days the days to set
     */
    public void setDays(String days) {
        this.days = days;
    }
    /**
     * @return the path
     */
    public Path getPath() {
        return path;
    }
    /**
     * @param path the path to set
     */
    public void setPath(Path path) {
        this.path = path;
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
        if (!(object instanceof Trip)) {
            return false;
        }
        Trip other = (Trip) object;
        return !((this.id == null && other.id != null)
                || (this.id != null && !this.id.equals(other.id)));
    }
    @Override
    public String toString() {
        return "ss.sonya.entity.Trip[ id=" + getId() + " ]";
    }
}
