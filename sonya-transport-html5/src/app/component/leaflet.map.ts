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
import {ElementRef} from '@angular/core'

import {TransportProfile, BusStop} from '../model/abs.model';

declare var L: any;

export abstract class LeafletMap {
    MOCK_BS: string = 'mock';
    EARTH_RAD: number = 6371; // km

    map: any;
    routing: any = this.createRoutingControl();
    ctxMenu: any;
    coords: any;
    layerBusStop: any = L.layerGroup([]);
    abstract createMarker(bs: BusStop): any;
    abstract createRoutingMarker(index: number, bs: any, total: number): any;
    createMap(profile: TransportProfile, container: ElementRef) {
        var map = L.map.Sonya(container.nativeElement, {
            southWest: L.latLng(profile.southWestLat, profile.southWestLon),
            northEast: L.latLng(profile.northEastLat, profile.northEastLon),
            bounds: L.latLngBounds(L.latLng(profile.southWestLat, profile.southWestLon),
                L.latLng(profile.northEastLat, profile.northEastLon)),
            maxBounds: L.latLngBounds(L.latLng(profile.southWestLat, profile.southWestLon),
                L.latLng(profile.northEastLat, profile.northEastLon)),
            minZoom: profile.minZoom,
        }, profile.centerLat, profile.centerLon, profile.initialZoom);
        this.createLayer().addTo(map);
        container.nativeElement.style.height = (window.innerHeight - 88) + 'px';
        map.invalidateSize(true);

        this.map = map;

        // map listeners
        var comp = this;
        var _map = this.map;
        map.on('click', function (e: any) {
            // prevent control clicks
            if (e.originalEvent && e.originalEvent.currentTarget
                && e.originalEvent.currentTarget
                && e.originalEvent.path) {
                // prev target
                var idx = e.originalEvent.path.indexOf(e.originalEvent.currentTarget) - 1;
                if (idx > 0 && e.originalEvent.path[idx]
                    && e.originalEvent.path[idx].className === 'leaflet-control-container') {
                    return;
                }
            }
            if (comp.ctxMenu) {
                if (comp.ctxMenu.isOpen()) {
                    _map.closePopup();
                } else {
                    comp.ctxMenu.setLatLng(e.latlng);
                    comp.ctxMenu.openOn(_map);
                    comp.setCurrentCoordinates(e.latlng);
                }
            }
        });
    }
    createLayer(): any {
        return L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
            id: 'osm.default'
        });
    }
    updateBusStopLayer(busstops: BusStop[]): void {
        var start = (new Date()).getTime();
        this.layerBusStop.clearLayers();
        console.log('total bus stops [' + busstops.length + ']');
        if (busstops.length === 0) {
            return;
        }
        var arrayElements = [];
        for (var i = 0; i < busstops.length; i++) {
            var m = this.createMarker(busstops[i]);
            arrayElements.push(m);
        }
        this.layerBusStop = L.layerGroup(arrayElements).addTo(this.map);
        console.log('draw bus stop layer elapsed time [' + ((new Date()).getTime() - start) + '] ms');
    }
    addControl(icon: string, onclick: Function, component: any, tooltip: string, pos: string) {
        var btn = L.DomUtil.create('button', 'l-control-btn');
        btn.innerHTML = '<i class="material-icons">' + icon + '</i>';
        btn.addEventListener('click', function () {
            onclick(component);
        });
        btn.setAttribute('title', tooltip);
        this.map.addControl(L.control.Sonya(btn, {position: pos}));
    }
    createContextMenu(width: number, items: CtxMenuItem[]): any {
        let _ctxMenu = L.popup({
            minWidth: width,
            maxWidth: width,
            className: 'l-ctx-menu',
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
        _ctxMenu.setContent(container);
        console.log('context menu init complete');
        return _ctxMenu;
    }
    createIcon(icName: string) {
        return L.icon({
            iconUrl: '/assets/image/' + icName + '.png',
            shadowUrl: null,
            iconSize: [24, 27],
            shadowSize: [0, 0], // size of the shadow
            iconAnchor: [12, 27], // point of the icon which will correspond to marker's location
            shadowAnchor: [0, 0], // the same for the shadow
            popupAnchor: [0, 0] // point from which the popup should open relative to the iconAnchor
        });
    }
    calcDistance(bs1: BusStop, bs2: BusStop) {
        let dLat: number = (bs2.latitude - bs1.latitude) * Math.PI / 180;
        let dLon: number = (bs2.longitude - bs1.longitude) * Math.PI / 180;
        let rLat1: number = (bs1.latitude) * Math.PI / 180;
        let rLat2: number = (bs2.latitude) * Math.PI / 180;
        let a: number = Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.sin(dLon / 2) * Math.sin(dLon / 2)
            * Math.cos(rLat1) * Math.cos(rLat2);
        let c: number = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return this.EARTH_RAD * c;
    }
    calcWayDistance(way: BusStop[]) {
        let sum = 0;
        let prev: BusStop = null;
        way.forEach(bs => {
            if (prev == null) {
                prev = bs;
            } else {
                if (this.MOCK_BS != bs.name) {
                    let dist = this.calcDistance(bs, prev);
                    sum += dist;
                    prev = bs;
                }
            }
        });
        return sum;
    }
    private createContextMenuBtn(container: any, item: CtxMenuItem): any {
        var btn = L.DomUtil.create('button', '', container);
        btn.setAttribute('type', 'button');
        btn.className = "l-context-menu-button";
        btn.innerHTML = '<i class="material-icons">'
            + item.icon + '</i> <span>' + item.label + '</span>';
        return btn;
    }
    private setCurrentCoordinates(coords: any): void {
        this.coords = coords;
    }
    private createRoutingControl() {
        let _routing = L.Routing.control({
            routeWhileDragging: false,
            draggableWaypoints: false,
            collapsible: true,
            show: false,
            fitSelectedRoutes: false,
            containerClassName: 'lrc',
            waypoints: [],
            createMarker: this.createRoutingMarker,
            lineOptions: {
                addWaypoints: false
            }
        });
        return _routing;
    }
}

export class CtxMenuItem {
    constructor(
        public icon: string,
        public label: string,
        public onclick: Function,
        public component: any
    ) {}
}

L.Map.Sonya = L.Map.extend({
    options: {
        zoomControl: false,
        keyboard: false,
        attributionControl: false
    },
    initialize: function (id: number, options: any, lat: number, lon: number, zoom: number) {
        L.Util.setOptions(this, options);
        L.Map.prototype.initialize.call(this, id, options);
        this.setView([lat, lon], zoom);
    }
});
L.map.Sonya = function (id: number, options: any, lat: number, lon: number, zoom: number) {
    return new L.Map.Sonya(id, options, lat, lon, zoom);
};

L.Control.Sonya = L.Control.extend({
    initialize: function (element: any, options: any) {
        L.Util.setOptions(this, options);
        this._element = element;
    },
    options: {
        position: 'topleft'
    },
    onAdd: function (map: any) {
        var container = L.DomUtil.create('div',
            'leaflet-control-layers leaflet-control l-contol');
        container.appendChild(this._element);
        return container;
    }
});
L.control.Sonya = function (element: any, options: any) {
    return new L.Control.Sonya(element, options);
};