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
package ss.sonya.transport.api;

import ss.sonya.entity.RouteProfile;
import ss.sonya.entity.TransportProfile;
import ss.sonya.transport.exception.DataDeserializationException;
import ss.sonya.transport.exception.DataSerializationException;

/**
 * Import data serializer / deserializer.
 * @author ss
 */
public interface ImportDataSerializer {
    /**
     * Serialize import data.
     * @param data import data.
     * @return binary data.
     * @throws DataSerializationException serialization error.
     */
    byte[] serialize(ImportData data) throws DataSerializationException;
    /**
     * Deserialize import data.
     * @param binData binary data.
     * @param transportProfile transport profile.
     * @param routeProfile route profile.
     * @return import data.
     * @throws DataDeserializationException deserialization error.
     */
    ImportData deserialize(byte[] binData, TransportProfile transportProfile,
            RouteProfile routeProfile) throws DataDeserializationException;
}
