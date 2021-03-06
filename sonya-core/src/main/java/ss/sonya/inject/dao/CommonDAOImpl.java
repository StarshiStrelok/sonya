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
package ss.sonya.inject.dao;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ss.sonya.inject.CommonDAO;

/**
 * Common DAO implementation.
 * @author ss
 */
@Repository
class CommonDAOImpl implements CommonDAO {
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(CommonDAOImpl.class);
    /** Persistence context. */
    @PersistenceContext
    private EntityManager em;
    @Override
    @Transactional(propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class)
    public <T> T create(final T entity) {
        em.persist(entity);
        return entity;
    }
    @Override
    @Transactional(propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class)
    public <T> T update(final T entity) {
        return em.merge(entity);
    }
    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public <T> T findById(final Serializable id, final Class<T> cl) {
        return em.find(cl, id);
    }
    @Override
    @Transactional(propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class)
    public <T> void delete(final Serializable id, final Class<T> cl) {
        em.remove(findById(id, cl));
    }
    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public <T> List<T> getAll(Class<T> cl) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<T> criteria = builder.createQuery(cl);
        Root<T> c = criteria.from(cl);
        criteria.select(c);
        Query query = em.createQuery(criteria);
        return query.getResultList();
    }
    @Override
    @Transactional(propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class)
    public <T> void createAll(final List<T> entities) {
        entities.stream().forEach(e -> {
            em.persist(e);
        });
    }
    @Override
    @Transactional(propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class)
    public <T> void updateAll(final List<T> entities) {
        entities.stream().forEach(e -> {
            em.merge(em.contains(e) ? e : em.merge(e));
        });
    }
    @Override
    @Transactional(propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class)
    public <T> void deleteAll(List<T> entities) {
        entities.stream().forEach(e -> {
            try {
                if (!em.contains(e)) {
                    Field f = e.getClass().getDeclaredField("id");
                    f.setAccessible(true);
                    Object id = f.get(e);
                    e = (T) em.find(entities.get(0).getClass(), id);
                    f.setAccessible(false);
                }
                em.remove(e);
            } catch (IllegalAccessException | IllegalArgumentException
                    | NoSuchFieldException | SecurityException ex) {
                LOG.warn("batch delete error!", ex);
            }
        });
    }
}
