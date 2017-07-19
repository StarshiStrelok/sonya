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
package ss.sonya.transport.rest;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ss.sonya.transport.api.ImportDataService;
import ss.sonya.transport.component.ImportDataEvent;

/**
 * General purposes web-service.
 * @author ss
 */
@RestController
@RequestMapping("/rest/data")
public class CommonWS {
    /** Import data service. */
    @Autowired
    private ImportDataService importDataService;
    /**
     * Import data.
     * @param tid transport profile ID.
     * @param rid route profile ID.
     * @param persist apply changes flag.
     * @param file file with data.
     * @return list events.
     * @throws Exception error.
     */
    @RequestMapping(value = "/import/{tid}/{rid}/{persist}",
            method = RequestMethod.POST)
    public List<ImportDataEvent> importData(@PathVariable("tid") Integer tid,
            @PathVariable("rid") Integer rid,
            @PathVariable("persist") boolean persist,
            @RequestBody MultipartFile file) throws Exception {
        return importDataService.importData(file.getBytes(), tid, rid, persist);
    }
}
