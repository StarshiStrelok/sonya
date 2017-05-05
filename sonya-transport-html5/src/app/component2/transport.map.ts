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

import {Component, OnInit, ViewChild, ElementRef} from '@angular/core';
import {MdSidenav} from '@angular/material';
import {NotificationsService} from 'angular2-notifications';

import {slideAnimation, AnimatedSlide} from './../app.component';
import {TransportProfile, ModelClass, SearchSettings, OptimalPath, Path} from '../model/abs.model';
import {DataService} from '../service/data.service';
import {CtxMenuItem} from '../model/ctx.menu.item';
import {GeoCoder} from './geocoder';
import {SearchTab} from './search.tab';
import {Waiting} from '../lib/material/waiting';
import {OSRMService} from '../service/osrm.service';
import {OSRMResponse} from '../model/osrm.response';

declare var L: any;

@Component({
    selector: 'transport-map',
    templateUrl: './transport.map.html',
    styleUrls: ['./transport.map.css'],
    animations: [slideAnimation]
})
export class TransportMap extends AnimatedSlide implements OnInit {
    @ViewChild('map') mapElement: ElementRef;
    @ViewChild('sidenav') sideNav: MdSidenav;
    @ViewChild(GeoCoder) geocoder: GeoCoder;
    @ViewChild(SearchTab) searchTabs: SearchTab;
    @ViewChild(Waiting) waiting: Waiting;
    map: any;
    mapMenu: any
    profiles: TransportProfile[];
    activeProfile: TransportProfile;
    layerEndpoint = new EndpointLayer();
    isMenuOpen: boolean = true;
    component: TransportMap = this;
    constructor(
        public dataService: DataService,
        public notificationService: NotificationsService,
        public osrmService: OSRMService
    ) {
        super();
    }

    ngOnInit() {
        this.dataService.getAll<TransportProfile>(ModelClass.TRANSPORT_PROFILE)
            .then((profiles: TransportProfile[]) => {
                this.profiles = profiles;
                this.activeProfile = this.profiles[0];
                this.createMap(this.activeProfile);
                this.mapMenu = this.createMapMenu(180, [
                    new CtxMenuItem('A', 'Start point',
                        this.fnMakeStartPoint, this),
                    new CtxMenuItem('B', 'End point',
                        this.fnMakeEndPoint, this)
                ]);
            });
    }
    fnMakeStartPoint(comp: TransportMap) {
        comp.setEndpoint(true);
    }
    fnMakeEndPoint(comp: TransportMap) {
        comp.setEndpoint(false);
    }
    setEndpoint(isStart: boolean) {
        let ll = this.mapMenu.getLatLng();
        if (isStart) {
            this.layerEndpoint.showStartMarker(ll.lat, ll.lng, false);
        } else {
            this.layerEndpoint.showEndMarker(ll.lat, ll.lng, false);
        }
        this.geocoder.reverseSearch(isStart, ll.lat, ll.lng);
    }
    openMenu() {
        this.isMenuOpen = true;
        this.sideNav.open().then(res => this.map.invalidateSize(true));
    }
    closeMenu() {
        this.isMenuOpen = false;
        this.sideNav.close().then(res => this.map.invalidateSize(true));
    }
    isMobile(): boolean {
        return window.innerWidth <= 600;
    }
    private createMap(profile: TransportProfile) {
        var map = L.map.Sonya(this.mapElement.nativeElement, {
            southWest: L.latLng(profile.southWestLat, profile.southWestLon),
            northEast: L.latLng(profile.northEastLat, profile.northEastLon),
            bounds: L.latLngBounds(L.latLng(profile.southWestLat, profile.southWestLon),
                L.latLng(profile.northEastLat, profile.northEastLon)),
            maxBounds: L.latLngBounds(L.latLng(profile.southWestLat, profile.southWestLon),
                L.latLng(profile.northEastLat, profile.northEastLon)),
            minZoom: profile.minZoom,
        }, profile.centerLat, profile.centerLon, profile.initialZoom);
        this.createLayer().addTo(map);
        this.mapElement.nativeElement.style.height = (window.innerHeight - 50) + 'px';
        map.invalidateSize(true);

        let comp = this;
        map.on('click', function (e: any) {
            if (comp.mapMenu) {
                if (comp.mapMenu.isOpen()) {
                    map.closePopup();
                } else {
                    comp.mapMenu.setLatLng(e.latlng);
                    comp.mapMenu.openOn(map);
                }
            }
        });
        this.map = map;
        this.layerEndpoint.init(this.map, this);
    }
    private createLayer(): any {
        return L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
            id: 'osm.default'
        });
    }
    private createMapMenu(width: number, items: CtxMenuItem[]): any {
        let _mapMenu = L.popup({
            minWidth: width,
            maxWidth: width,
            className: 'l-ctx-menu l-ctx-menu-offset',
            offset: [90, 84],
            closeButton: false,
            closeOnClick: false
        })
        var container = L.DomUtil.create('div');
        var _map = this.map;
        items.forEach((item: CtxMenuItem) => {
            let btn = this.createContextMenuBtn(container, item);
            L.DomEvent.on(btn, 'click', function () {
                _map.closePopup();
                item.onclick(item.component);
            });
        });
        _mapMenu.setContent(container);
        return _mapMenu;
    }
    private createContextMenuBtn(container: any, item: CtxMenuItem): any {
        var btn = L.DomUtil.create('button', '', container);
        btn.setAttribute('type', 'button');
        btn.className = "l-context-menu-button";
        btn.innerHTML = '<span class="' + (item.icon === 'A' ? 'letter-a' : 'letter-b') + '">'
            + item.icon + '</span>'
            + '<span class> ' + item.label + '</span>';
        return btn;
    }
}

export class EndpointLayer {
    layerEndpoint: any;
    startMarker: any;
    endMarker: any;
    map: any;
    parent: TransportMap;
    public searchRouteCtrl = new SearchRoute();
    init(map: any, parent: TransportMap) {
        this.map = map;
        this.parent = parent;
        this.searchRouteCtrl.init(this.parent);
        this.layerEndpoint = L.layerGroup([]);
        this.layerEndpoint.addTo(map);
        this.startMarker = this.createMarker(true);
        this.endMarker = this.createMarker(false);
    }
    showStartMarker(lat: number, lon: number, isMove: boolean) {
        this.startMarker.setLatLng(new L.LatLng(lat, lon));
        if (!this.layerEndpoint.hasLayer(this.startMarker)) {
            this.layerEndpoint.addLayer(this.startMarker);
        }
        if (isMove) {
            this.moveToMarker(lat, lon);
        }
        this.checkSearchConditions();
    }
    showEndMarker(lat: number, lon: number, isMove: boolean) {
        this.endMarker.setLatLng(new L.LatLng(lat, lon));
        if (!this.layerEndpoint.hasLayer(this.endMarker)) {
            this.layerEndpoint.addLayer(this.endMarker);
        }
        if (isMove) {
            this.moveToMarker(lat, lon);
        }
        this.checkSearchConditions();
    }
    hideStartMarker() {
        if (this.layerEndpoint.hasLayer(this.startMarker)) {
            this.layerEndpoint.removeLayer(this.startMarker);
        }
    }
    hideEndMarker() {
        if (this.layerEndpoint.hasLayer(this.endMarker)) {
            this.layerEndpoint.removeLayer(this.endMarker);
        }
    }
    checkSearchConditions() {
        if (this.layerEndpoint.hasLayer(this.startMarker)
            && this.layerEndpoint.hasLayer(this.endMarker)) {
            this.searchRouteCtrl.search();
        }
    }
    private createMarker(isStart: boolean): any {
        var marker = L.marker(new L.LatLng(0, 0), {
            icon: isStart
                ? this.createIcon('start') : this.createIcon('end'),
            clickable: true,
            draggable: true,
            title: isStart ? 'Start' : 'Finish',
            isStart: isStart
        });
        var comp = this;
        marker.on('dragend', function (e: any) {
            let ll = marker.getLatLng();
            if (e.target.options.isStart) {
                comp.parent.geocoder.reverseSearch(true, ll.lat, ll.lng);
            } else {
                comp.parent.geocoder.reverseSearch(false, ll.lat, ll.lng);
            }
            comp.checkSearchConditions();
        });
        return marker;
    }
    private createIcon(icName: string) {
        return L.icon({
            iconUrl: '/assets/image/' + icName + '.png',
            shadowUrl: '/assets/image/shadow.png',
            iconSize: [32, 37],
            shadowSize: [39, 27],
            iconAnchor: [16, 37],
            shadowAnchor: [12, 27],
            popupAnchor: [0, 0]
        });
    }
    private moveToMarker(lat: number, lon: number) {
        this.map.flyTo(new L.LatLng(lat, lon), 16, {
            animate: true
        });
    }
}

export class SearchRoute {
    private layerRouting: any[];
    parent: TransportMap
    init(parent: TransportMap) {
        this.parent = parent;
    }
    search() {
        let settings: SearchSettings = this.parent.searchTabs.searchSettings.getSettings();
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
                this.parent.waiting.close();
                this.parent.searchTabs.searchResult.setResult(res);
                console.log(res);
            });
    }
    drawRoute(optimalPath: OptimalPath) {
        // clear previous route
        if (this.layerRouting) {
            this.layerRouting.forEach(layer => layer.clearLayers());
        }
        this.layerRouting = [L.layerGroup([]), L.layerGroup([]), L.layerGroup([])];
        let counter = 0;
        let max = optimalPath.path.length;
        let _comp = this;
        let groups: any[] = [[], [], []];
        let addPathLayer = function (path: Path, resp: OSRMResponse) {
            let lineColor = path.route.type.lineColor;
            var legs = resp.routes[0].legs;
            let reverseCoords: number[][] = [];
            legs.forEach(leg => {
                leg.steps.forEach(step => {
                    step.geometry.coordinates.forEach(ll => {
                        reverseCoords.push([ll[1], ll[0]]);
                    });
                });
            });
            var opts = [{
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
                snaking: true
            }];
            for (let i = 0; i < opts.length; i++) {
                groups[i].push(L.polyline(reverseCoords, opts[i]));
            }
        }
        let request = function () {
            let way = optimalPath.way[counter];
            let path = optimalPath.path[counter];
            _comp.parent.osrmService.requestPath(way, path.route.type.routingURL).then(resp => {
                addPathLayer(path, resp);
                if (++counter < max) {
                    request();
                } else {
                    for (let i = 0; i < groups.length; i++) {
                        groups[i].forEach((el: any) => {_comp.layerRouting[i].addLayer(el)});
                    }
                    _comp.layerRouting.forEach(layer => layer.addTo(_comp.parent.map).snakeIn());
                }
            });
        }
        request();
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
