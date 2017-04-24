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
package ss.sonya.inject.service;

import java.io.Serializable;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import ss.sonya.inject.CommonDAO;
import ss.sonya.inject.DataService;

/**
 * Data service implementation.
 * @author ss
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
class DataServiceImpl implements DataService {
    /** Common DAO. */
    @Autowired
    private CommonDAO commonDAO;
    @Override
    public <T> T create(T entity) throws Exception {
        return commonDAO.create(entity);
    }
    @Override
    public <T> T update(T entity) throws Exception {
        return commonDAO.update(entity);
    }
    @Override
    public <T> T findById(Serializable id, Class<T> cl) throws Exception {
        return commonDAO.findById(id, cl);
    }
    @Override
    public <T> void delete(Serializable id, Class<T> cl) throws Exception {
        commonDAO.delete(id, cl);
    }
    @Override
    public <T> List<T> getAll(Class<T> cl) throws Exception {
        return commonDAO.getAll(cl);
    }
    @Override
    public <T> void createAll(List<T> entities) throws Exception {
        commonDAO.createAll(entities);
    }
    @Override
    public <T> void updateAll(List<T> entities) throws Exception {
        commonDAO.updateAll(entities);
    }
    @Override
    public <T> void deleteAll(List<T> entities) throws Exception {
        commonDAO.deleteAll(entities);
    }
}
