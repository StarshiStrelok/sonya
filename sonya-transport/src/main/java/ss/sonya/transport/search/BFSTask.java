/*
 * Copyright (C) 2017 ss
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import org.apache.log4j.Logger;
import ss.sonya.entity.BusStop;
import ss.sonya.transport.search.vo.Decision;

/**
 * BFS task.
 * @author ss
 */
public class BFSTask implements Callable<List<Decision>> {
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(BFSTask.class);
    /** Start vertex. */
    private final int sV;
    /** End bus stops. */
    private final Map<Integer, Set<BusStop>> endVertices;
    /** Start bus stops. */
    private final Set<BusStop> startBusStops;
    /** Graph. */
    private final Graph graph;
    /**
     * Constructor.
     * @param startVertex start vertex.
     * @param pEndVertices end bus stops.
     * @param pStartBusStops start bus stops.
     * @param pGraph graph.
     */
    public BFSTask(final int startVertex,
            final Map<Integer, Set<BusStop>> pEndVertices,
            final Set<BusStop> pStartBusStops, final Graph pGraph) {
        sV = startVertex;
        endVertices = pEndVertices;
        startBusStops = pStartBusStops;
        graph = pGraph;
    }
    @Override
    public List<Decision> call() throws Exception {
        return bfs();
    }
    /**
     * BFS implementation.
     * @return list decisions for depth less or equals limitDepth.
     * @throws Exception method error.
     */
    private List<Decision> bfs() throws Exception {
        List<Decision> result = new ArrayList<>();
        Set<Integer> endCriteria = endVertices.keySet();
        
        return result;
    }
}
