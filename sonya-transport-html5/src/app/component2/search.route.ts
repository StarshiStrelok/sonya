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

import {SearchSettings, OptimalPath,
    Path, BusStop, RouteProfile} from '../model/abs.model';
import {TransportMap} from './transport.map';
import {OSRMResponse} from '../model/osrm.response';

declare var L: any;

export class SearchRoute {
    private layerRoutingStatic = L.layerGroup([]);
    private layerRoutingDynamic = L.layerGroup([]);
    parent: TransportMap
    animateSpeed: number = 500; // px / sec
    init(parent: TransportMap) {
        this.parent = parent;
        this.layerRoutingStatic.addTo(this.parent.map);
        this.layerRoutingDynamic.addTo(this.parent.map);
    }
    search() {
        let settings: SearchSettings = this.parent.searchTabs.searchSettings.getSettingsValues();
        let startll = this.parent.layerEndpoint.startMarker.getLatLng();
        let endll = this.parent.layerEndpoint.endMarker.getLatLng();
        settings.profileId = this.parent.activeProfile.id;
        settings.startLat = startll.lat;
        settings.startLon = startll.lng;
        settings.endLat = endll.lat;
        settings.endLon = endll.lng;
        this.parent.waiting.open();
        this.parent.dataService.searchRoutes(settings, this.parent.waiting)
            .then((res: OptimalPath[]) => {
                if (res.length === 0) {
                    this.parent.notificationService.info(
                        this.parent.translate.instant('transport-map.search-result.not-found.title'),
                        this.parent.translate.instant('transport-map.search-result.not-found.body')
                    );
                }
                this.parent.waiting.close();
                this.parent.searchTabs.searchResult.setResult(res);
                this.parent.searchTabs.searchResult.closeDetails();
                this.parent.searchTabs.tabGroup.selectedIndex = 0;
            });
    }
    clearRoutes() {
        this.layerRoutingStatic.clearLayers();
        this.layerRoutingDynamic.clearLayers();
    }
    drawRoute(optimalPath: OptimalPath) {
        // clear previous route
        this.layerRoutingStatic.clearLayers();
        this.layerRoutingDynamic.clearLayers();
        // flying
        let all: BusStop[] = [];
        optimalPath.way.forEach(w => {
            w.forEach(bs => {
                all.push(bs); 
            });
        });
        this.flyToBounds(all, 0);
        // polyline
        let counter = 0;
        let max = optimalPath.path.length;
        let _comp = this;
        let groupD: any[] = [];
        let groupS: any[] = [];
        let prevPath: Path = null;
        let addPathLayer = function (path: Path, way: BusStop[], resp: OSRMResponse) {
            let lineColor = path.route.type.lineColor;
            let legs = resp.routes[0].legs;
            path['distance'] = (resp.routes[0].distance / 1000).toFixed(1);
            path['duration'] = resp.routes[0].duration;
            let reverseCoords: number[][] = [];
            legs.forEach(leg => {
                leg.steps.forEach(step => {
                    step.geometry.coordinates.forEach(ll => {
                        reverseCoords.push([ll[1], ll[0]]);
                    });
                });
            });
            var opts: any = [{
                color: 'black',
                opacity: 0.15,
                weight: 9
            }, {
                color: 'white',
                opacity: 0.8,
                weight: 6
            }, {
                color: lineColor ? lineColor : 'red',
                opacity: 1,
                weight: 2,
                snakingSpeed: _comp.animateSpeed,
                snaking: true
            }];
            if (prevPath !== null && path.route.type.name === prevPath.route.type.name
                    && !prevPath['dashed']) {
                opts[2].dashArray = '5,5';
                path['dashed'] = true;
            }
//            if (prevPath != null) {
//                opts.forEach((options: any) => {
//                    groupD.push(L.circleMarker(new L.LatLng(way[0].latitude, way[0].longitude), options));
//                });
//            }
            for (let i = 0; i < opts.length; i++) {
                if (opts[i].snaking) {
                    groupD.push(L.polyline(reverseCoords, opts[i]));
                } else {
                    groupS.push(L.polyline(reverseCoords, opts[i]));
                }
            }
            way.forEach(bs => {
                groupS.push(_comp.createMarker(bs, path.route.type));
            });
            prevPath = path;
        }
        let request = function () {
            let way = optimalPath.way[counter];
            let path = optimalPath.path[counter];
            _comp.parent.osrmService.requestPath(way, path.route.type.routingURL).then(resp => {
                addPathLayer(path, way, resp);
                if (++counter < max) {
                    request();
                } else {
                    _comp.parent.map.removeLayer(_comp.layerRoutingDynamic);    // for fix animation bug
                    _comp.layerRoutingDynamic = L.layerGroup([]);
                    groupS.forEach(layer => _comp.layerRoutingStatic.addLayer(layer));
                    groupD.forEach(layer => _comp.layerRoutingDynamic.addLayer(layer));
                    _comp.layerRoutingDynamic.addTo(_comp.parent.map).snakeIn();
                }
            });
        }
        request();  // draw polyline
    }
    flyToBounds(markers: BusStop[], paddingX: number) {
        if (markers.length === 0) {
            return;
        }
        this.parent.map.flyToBounds(this.getMarkersBounds(markers), {
            animate: true,
            duration: 0.3,
            easeLinearity: 0.25,
            paddingTopLeft: [paddingX ? paddingX : 0, 0]
        });
    }
    private createMarker(bs: BusStop, routeType: RouteProfile): any {
        var marker = L.marker(new L.LatLng(bs.latitude, bs.longitude), {
            icon: bs.name === this.parent.MOCK_BS
                ? this.createIconMock() : this.createIcon(routeType.id),
            clickable: true,
            draggable: false,
            title: bs.name === this.parent.MOCK_BS ? '' : bs.name
        });
        marker.info = bs;
        return marker;
    }
    private createIcon(typeId: number) {
        return L.icon({
            iconUrl: '/rest/data/transport-profile/route/marker/' + typeId,
            shadowUrl: '/assets/image/shadow.png',
            iconSize: [24, 27],
            shadowSize: [39, 27],
            iconAnchor: [12, 27],
            shadowAnchor: [12, 27],
            popupAnchor: [0, 0]
        });
    }
    private createIconMock() {
        return L.icon({
            iconUrl: '/assets/image/mock.png',
            shadowUrl: null,
            iconSize: [0, 0],
            shadowSize: [0, 0],
            iconAnchor: [0, 0],
            shadowAnchor: [0, 0],
            popupAnchor: [0, 0]
        });
    }
    private getMarkersBounds(markers: BusStop[]) {
        var _sw_lat;
        var _sw_lon;
        var _ne_lat;
        var _ne_lon;
        var _delta = 0.003;
        for (var i = 0; i < markers.length; i++) {
            var m = markers[i];
            var lat = m.latitude;
            var lon = m.longitude;
            lon = lon ? lon : m.longitude;
            if (!_sw_lat || _sw_lat > lat) {
                _sw_lat = lat;
            }
            if (!_sw_lon || _sw_lon > lon) {
                _sw_lon = lon;
            }
            if (!_ne_lat || _ne_lat < lat) {
                _ne_lat = lat;
            }
            if (!_ne_lon || _ne_lon < lon) {
                _ne_lon = lon;
            }
        }
        var bounds = [
            [_sw_lat - _delta, _sw_lon - _delta],
            [_ne_lat + _delta, _ne_lon + _delta]
        ];
        return bounds;
    }
}


// ============================== POLYLINE ANIMATION ==========================
// FROM https://github.com/IvanSanchez/Leaflet.Polyline.SnakeAnim
//
///// FIXME: Use path._rings instead of path._latlngs???
///// FIXME: Panic if this._map doesn't exist when called.
///// FIXME: Implement snakeOut()
///// FIXME: Implement layerGroup.snakeIn() / Out()
L.Polyline.include({
    // Hi-res timestamp indicating when the last calculations for vertices and
    // distance took place.
    _snakingTimestamp: 0,
    // How many rings and vertices we've already visited
    // Yeah, yeah, "rings" semantically only apply to polygons, but L.Polyline
    // internally uses that nomenclature.
    _snakingRings: 0,
    _snakingVertices: 0,
    // Distance to draw (in screen pixels) since the last vertex
    _snakingDistance: 0,
    // Flag
    _snaking: false,
    /// TODO: accept a 'map' parameter, fall back to addTo() in case
    /// performance.now is not available.
    snakeIn: function () {
        if (this._snaking) {return;}

        if (!('performance' in window) ||
            !('now' in window.performance) ||
            !this._map) {
            return;
        }
        this._snaking = true;
        this._snakingTime = performance.now();
        this._snakingVertices = this._snakingRings = this._snakingDistance = 0;

        if (!this._snakeLatLngs) {
            this._snakeLatLngs = L.Polyline._flat(this._latlngs) ?
                [this._latlngs] :
                this._latlngs;
        }
        // Init with just the first (0th) vertex in a new ring
        // Twice because the first thing that this._snake is is chop the head.
        this._latlngs = [[this._snakeLatLngs[0][0], this._snakeLatLngs[0][0]]];

        this._update();
        this._snake();
        this.fire('snakestart');
        return this;
    },
    _snake: function () {

        var now = performance.now();
        var diff = now - this._snakingTime;	// In milliseconds
        var forward = diff * this.options.snakingSpeed / 1000;	// In pixels
        this._snakingTime = now;

        // Chop the head from the previous frame
        this._latlngs[this._snakingRings].pop();

        return this._snakeForward(forward);
    },
    _snakeForward: function (forward) {
        // Calculate distance from current vertex to next vertex
        try {
            var currPoint = this._map.latLngToContainerPoint(
                this._snakeLatLngs[this._snakingRings][this._snakingVertices]);
        } catch (err) {
            return;
        }
        var nextPoint = this._map.latLngToContainerPoint(
            this._snakeLatLngs[this._snakingRings][this._snakingVertices + 1]);
        var distance = currPoint.distanceTo(nextPoint);

        // 		console.log('Distance to next point:', distance, '; Now at: ', this._snakingDistance, '; Must travel forward:', forward);
        // 		console.log('Vertices: ', this._latlngs);

        if (this._snakingDistance + forward > distance) {
            // Jump to next vertex
            this._snakingVertices++;
            this._latlngs[this._snakingRings].push(this._snakeLatLngs[this._snakingRings][this._snakingVertices]);

            if (this._snakingVertices >= this._snakeLatLngs[this._snakingRings].length - 1) {
                if (this._snakingRings >= this._snakeLatLngs.length - 1) {
                    return this._snakeEnd();
                } else {
                    this._snakingVertices = 0;
                    this._snakingRings++;
                    this._latlngs[this._snakingRings] = [
                        this._snakeLatLngs[this._snakingRings][this._snakingVertices]
                    ];
                }
            }

            this._snakingDistance -= distance;
            return this._snakeForward(forward);
        }
        this._snakingDistance += forward;
        var percent = this._snakingDistance / distance;
        var headPoint = nextPoint.multiplyBy(percent).add(
            currPoint.multiplyBy(1 - percent)
        );
        // Put a new head in place.
        var headLatLng = this._map.containerPointToLatLng(headPoint);
        this._latlngs[this._snakingRings].push(headLatLng);

        this.setLatLngs(this._latlngs);
        this.fire('snake');
        L.Util.requestAnimFrame(this._snake, this);
    },

    _snakeEnd: function () {

        this.setLatLngs(this._snakeLatLngs);
        this._snaking = false;
        this.fire('snakeend');

    }

});

L.Polyline.mergeOptions({
    snakingSpeed: 200	// In pixels/sec
});

L.LayerGroup.include({
    _snakingLayers: [],
    _snakingLayersDone: 0,
    snakeIn: function () {

        if (!('performance' in window) ||
            !('now' in window.performance) ||
            !this._map ||
            this._snaking) {
            return;
        }
        this._snaking = true;
        this._snakingLayers = [];
        this._snakingLayersDone = 0;
        var keys = Object.keys(this._layers);
        for (var i in keys) {
            var key = keys[i];
            this._snakingLayers.push(this._layers[key]);
        }
        this.clearLayers();

        this.fire('snakestart');
        return this._snakeNext();
    },
    _snakeNext: function () {
        if (this._snakingLayersDone >= this._snakingLayers.length) {
            this.fire('snakeend');
            this._snaking = false;
            return;
        }
        var currentLayer = this._snakingLayers[this._snakingLayersDone];
        this._snakingLayersDone++;
        this.addLayer(currentLayer);
        if ('snakeIn' in currentLayer) {
            currentLayer.once('snakeend', function () {
                setTimeout(this._snakeNext.bind(this), this.options.snakingPause);
            }, this);
            currentLayer.snakeIn();
        } else {
            setTimeout(this._snakeNext.bind(this), this.options.snakingPause);
        }
        this.fire('snake');
        return this;
    }

});

L.LayerGroup.mergeOptions({
    snakingPause: 200
});
