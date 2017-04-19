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
import ss.sonya.entity.BusStop;
import ss.sonya.transport.api.BusStopDAO;
import ss.sonya.transport.api.BusStopService;

/**
 * Bus stop service implementation.
 * @author ss
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
class BusStopServiceImpl implements BusStopService {
    /** Bus stop DAO. */
    @Autowired
    private BusStopDAO busStopDAO;
    @Override
    public List<BusStop> getProfileBusStops(final Integer pid)
            throws Exception {
        return busStopDAO.getProfileBusStops(pid);
    }
}
