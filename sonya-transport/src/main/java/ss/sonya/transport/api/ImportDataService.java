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

import java.util.List;
import ss.sonya.transport.component.ImportDataEvent;
import ss.sonya.transport.exception.ImportDataException;

/**
 * Import data service.
 * @author ss
 */
public interface ImportDataService {
    /**
     * Import data.
     * @param file uploaded file with data.
     * @param tpId transport profile ID.
     * @param rtID route profile ID.
     * @param isPersist persist changes flag, if false - changes not committed.
     * @param reloadGraph reload graph after import.
     * @return list changes.
     * @throws ImportDataException import error.
     */
    List<ImportDataEvent> importData(byte[] file,
            Integer tpId, Integer rtID, boolean isPersist, boolean reloadGraph)
            throws ImportDataException;
    /**
     * Global data update.
     * May be run as scheduled.
     */
    void globalUpdate();
}
