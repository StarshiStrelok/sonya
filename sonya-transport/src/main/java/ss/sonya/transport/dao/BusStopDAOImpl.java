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
package ss.sonya.transport.dao;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ss.sonya.entity.BusStop;
import ss.sonya.transport.api.BusStopDAO;

/**
 * Bus stop DAO implementation.
 * @author ss
 */
@Repository
class BusStopDAOImpl implements BusStopDAO {
    /** Persistence context. */
    @PersistenceContext
    private EntityManager em;
    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public List<BusStop> getProfileBusStops(Integer id) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<BusStop> criteria = builder.createQuery(BusStop.class);
        Root<BusStop> c = criteria.from(BusStop.class);
        criteria.select(c).where(
                builder.equal(c.get("transportProfile").get("id"), id)
        );
        Query query = em.createQuery(criteria);
        return query.getResultList();
    }
}
