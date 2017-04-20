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
package ss.sonya.transport.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import ss.sonya.entity.Path;
import ss.sonya.entity.Route;
import ss.sonya.transport.api.TransportDataDAO;
import ss.sonya.transport.api.TransportDataService;

/**
 * Transport data service implementation.
 * @author ss
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
class TransportDataServiceImpl implements TransportDataService {
    /** Bus stop DAO. */
    @Autowired
    private TransportDataDAO transportDAO;
    @Override
    public <T> List<T> getFromProfile(Integer id, Class<T> cl)
            throws Exception {
        return transportDAO.getFromProfile(id, cl);
    }
    @Override
    public List<Route> getRoutesFromSameType(Integer id) throws Exception {
        return transportDAO.getRoutesFromSameType(id);
    }
    @Override
    public List<Path> getPathsFromRoute(Integer id) throws Exception {
        return transportDAO.getPathsFromRoute(id);
    }
}
