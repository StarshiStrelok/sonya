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

import {TransportMap} from './transport.map';
import {SearchRoute} from './search.route';

declare var L: any;

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
