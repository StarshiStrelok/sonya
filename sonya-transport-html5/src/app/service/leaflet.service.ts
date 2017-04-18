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

import {Injectable, ElementRef} from '@angular/core';
import {TransportProfile} from '../model/transport-profile';

declare var L: any;

@Injectable()
export class LeafletService {
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
        container.nativeElement.style.height = (window.innerHeight - 138) + 'px';
        map.invalidateSize(true);
        return map;
    }
    createLayer(): any {
        return L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
            id: 'osm.default'
        });
    }
    addControl(map: any, icon: string, onclick: Function, component: any, tooltip: string) {
        var btn = L.DomUtil.create('button', 'l-control-btn');
        btn.innerHTML = '<i class="material-icons">' + icon + '</i>';
        btn.addEventListener('click', function(){
            onclick(component);
        });
        btn.setAttribute('title', tooltip);
        map.addControl(L.control.Sonya(btn, {position: 'topright'}));
    }
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
        console.log(this._element);
        var container = L.DomUtil.create('div',
                'leaflet-control-layers leaflet-control l-contol');
        container.appendChild(this._element);
        return container;
    }
});
L.control.Sonya = function (element: any, options: any) {
    return new L.Control.Sonya(element, options);
};
