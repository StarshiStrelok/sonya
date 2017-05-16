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

import java.util.LinkedList;
import java.util.List;
import ss.sonya.entity.Path;

/**
 * Metro graph.
 * @author ss
 */
public class MetroGraph {
    /** Path in vertex order. */
    private final List<Path> paths;
    /** Lists of adjacency. */
    private final List<Integer[]>[] adj;
    /** Edges count. */
    private int edges;
    /**
     * Constructor.
     * @param sortedPaths sorted paths.
     */
    public MetroGraph(final List<Path> sortedPaths) {
        adj = new List[sortedPaths.size()];
        paths = sortedPaths;
        edges = 0;
        for (int i = 0; i < adj.length; i++) {
            adj[i] = new LinkedList<>();
        }
    }
}
