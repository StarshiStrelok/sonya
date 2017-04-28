/*
 * Copyright (C) 2016 ss
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package ss.sonya.transport.search;

import java.util.List;
import ss.sonya.transport.search.vo.OptimalPath;
import ss.sonya.transport.search.vo.SearchSettings;

/**
 * Search route engine.
 * @author ss
 */
public interface SearchEngine {
    /**
     * Find paths in transport graph.
     * @param settings search settings.
     * @return list optimal paths.
     * @throws Exception operation error.
     */
    List<OptimalPath> search(SearchSettings settings) throws Exception;
}
