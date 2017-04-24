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
package ss.sonya.transport.component;

import java.util.HashMap;
import java.util.Map;
import ss.sonya.transport.constants.ImportDataEventType;
import ss.sonya.transport.constants.ImportInfoKey;

/**
 * Import data event.
 * @author ss
 */
public class ImportDataEvent {
    /** Trigger entity. */
    private String trigger;
    /** Event type. */
    private ImportDataEventType type;
    /** Event info. */
    private Map<ImportInfoKey, String> info = new HashMap<>();
    /**
     * @return the trigger
     */
    public String getTrigger() {
        return trigger;
    }
    /**
     * @param trigger the trigger to set
     */
    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }
    /**
     * @return the type
     */
    public ImportDataEventType getType() {
        return type;
    }
    /**
     * @param type the type to set
     */
    public void setType(ImportDataEventType type) {
        this.type = type;
    }
    /**
     * @return the info
     */
    public Map<ImportInfoKey, String> getInfo() {
        return info;
    }
    /**
     * @param info the info to set
     */
    public void setInfo(Map<ImportInfoKey, String> info) {
        this.info = info;
    }
}
