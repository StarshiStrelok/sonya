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
import org.springframework.web.multipart.MultipartFile;
import ss.sonya.entity.MapLayer;
import ss.sonya.entity.Path;
import ss.sonya.entity.Route;
import ss.sonya.entity.RouteProfile;
import ss.sonya.entity.RouteProfileMarkerImage;
import ss.sonya.entity.Trip;
import ss.sonya.inject.CommonDAO;
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
    /** Common DAO. */
    @Autowired
    private CommonDAO commonDAO;
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
    @Override
    public List<Trip> getSchedule(Integer id) throws Exception {
        return transportDAO.getSchedule(id);
    }
    @Override
    public byte[] getRouteTypeBusStopMarker(Integer id) throws Exception {
        RouteProfileMarkerImage image = transportDAO
                .getRouteTypeBusStopMarker(id);
        return image == null ? null : image.getData();
    }
    @Override
    public void uploadRouteTypeBusStopMarker(
            Integer id, MultipartFile file) throws Exception {
        RouteProfileMarkerImage image = transportDAO
                .getRouteTypeBusStopMarker(id);
        if (image == null) {
            image = new RouteProfileMarkerImage();
            image.setData(file.getBytes());
            image.setProfile(commonDAO.findById(id, RouteProfile.class));
            commonDAO.create(image);
        } else {
            image.setData(file.getBytes());
            commonDAO.update(image);
        }
    }
    @Override
    public byte[] getMapLayerIcon(Integer id) throws Exception {
        return transportDAO.getMapLayerIcon(id);
    }
    @Override
    public void uploadMapLayerIcon(Integer id, MultipartFile file)
            throws Exception {
        MapLayer layer = commonDAO.findById(id, MapLayer.class);
        layer.setIcon(file.getBytes());
        commonDAO.update(layer);
    }
}
