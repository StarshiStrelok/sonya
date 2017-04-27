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

import {slideAnimation, AnimatedSlide} from './../app.component';
import {TransportProfile, ModelClass} from '../model/abs.model';
import {DataService} from '../service/data.service';

declare var L: any;

@Component({
    selector: 'transport-map',
    templateUrl: './transport.map.html',
    styleUrls: ['./transport.map.css'],
    animations: [slideAnimation]
})

export class TransportMap extends AnimatedSlide implements OnInit {
    @ViewChild('map') mapElement: ElementRef;
    map: any;
    profiles: TransportProfile[];
    constructor(private dataService: DataService) {
        super();
    }
    
    ngOnInit() {
        this.dataService.getAll<TransportProfile>(ModelClass.TRANSPORT_PROFILE)
            .then((profiles: TransportProfile[]) => {
                this.profiles = profiles;
                this.createMap(this.profiles[0]);
            });
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
        this.mapElement.nativeElement.style.height = (window.innerHeight) + 'px';
        map.invalidateSize(true);

        this.map = map;
    }
    private createLayer(): any {
        return L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
            id: 'osm.default'
        });
    }
}
