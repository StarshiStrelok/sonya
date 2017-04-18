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

import {Injectable} from '@angular/core';
import {TransportProfile} from '../model/transport-profile';

declare var L: any;

@Injectable()
export class LeafletService {
    createMap(id: string, profile: TransportProfile): any {
        var map = L.map.Sonya(id, {
            zoomControl: false,
            attributionControl: false,
            southWest: L.latLng(profile.southWestLat, profile.southWestLon),
            northEast: L.latLng(profile.northEastLat, profile.northEastLon),
            bounds: L.latLngBounds(L.latLng(profile.southWestLat, profile.southWestLon),
                L.latLng(profile.northEastLat, profile.northEastLon)),
            maxBounds: L.latLngBounds(L.latLng(profile.southWestLat, profile.southWestLon),
                L.latLng(profile.northEastLat, profile.northEastLon)),
            minZoom: profile.minZoom,
        }, profile.centerLat, profile.centerLon, profile.initialZoom);
        
        this.createLayer().addTo(map);
        
        map.initControls([{}], 'topright');
        
        document.getElementById(id).style.height = (window.innerHeight - 200) + 'px';
        map.invalidateSize(true);
        
        return map;
    }
    createLayer(): any {
        return L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
            id: 'osm.default'
        });
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
    },
    initControls: function (controls: any, position: string) {
        for (var i = 0; i < controls.length; i++) {
            controls[i].options = {
                position: position
            };
            this.addControl(L.control.Sonya(controls[i]));
        }
    }
});
L.map.Sonya = function (id: number, options: any, lat: number, lon: number, zoom: number) {
    return new L.Map.Sonya(id, options, lat, lon, zoom);
};

L.Control.Sonya = L.Control.extend({
    initialize: function (control: any, position: string) {
        L.Util.setOptions(this, control.options);
        this._position = position;
    },
    options: {
        position: this._position
    },
    onAdd: function (map: any) {
        var container = L.DomUtil.create('div',
                'leaflet-control-layers leaflet-control');
        var btn = L.DomUtil.create('button', '');
        container.appendChild(btn);
        return container;
    }
});
L.control.Sonya = function (control: any) {
    return new L.Control.Sonya(control);
};