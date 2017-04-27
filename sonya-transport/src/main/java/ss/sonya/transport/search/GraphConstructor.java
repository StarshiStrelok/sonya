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
package ss.sonya.transport.search;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

/**
 * Graph constructor.
 * Build all graphs from transport profiles.
 * @author ss
 */
@Service
public class GraphConstructor {
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(GraphConstructor.class);
    /** All graphs, key - transport profile ID. */
    private static final Map<Integer, Graph> GRAPHS = new ConcurrentHashMap<>();
    
}
