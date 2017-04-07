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
import javax.annotation.Resource;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
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
    /** Hibernate session factory. */
    @Resource(name = "sessionFactory")
    private SessionFactory sessionFactory;
    @Override
    @Transactional(propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class)
    public <T> T create(final T entity) {
        sessionFactory.getCurrentSession().save(entity);
        return entity;
    }
    @Override
    @Transactional(propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class)
    public <T> T update(final T entity) {
        Session session = sessionFactory.getCurrentSession();
        return (T) session.merge(entity);
    }
    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public <T> T findById(final Serializable id, final Class<T> cl) {
        return sessionFactory.getCurrentSession().get(cl, id);
    }
    @Override
    @Transactional(propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class)
    public <T> void delete(final Serializable id, final Class<T> cl) {
        Session session = sessionFactory.getCurrentSession();
        session.delete(session.get(cl, id));
    }
}
