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

import {Component, OnInit, Input} from '@angular/core';
import {Http} from '@angular/http';
import {NotificationsService} from 'angular2-notifications';

import {TransportProfile} from '../model/abs.model';
import {EndpointLayer} from '../component2/transport.map';

@Component({
    selector: 'geocoder',
    templateUrl: './geocoder.html',
    styleUrls: ['./geocoder.css']
})
export class GeoCoder implements OnInit {
    GEOCODER_URL: string = 'http://nominatim.openstreetmap.org/search/';
    start: string;
    end: string;
    startPositions: Response[] = [];
    endPositions: Response[] = [];
    @Input() profile: TransportProfile;
    @Input() layerEndpoint: EndpointLayer;
    constructor(
        private http: Http,
        private notificationService: NotificationsService
    ) {}
    ngOnInit() {
        
    }
    pressStart(event: any) {
        if (event.keyCode === 13) {
            this.search(true);
        }
    }
    pressEnd(event: any) {
        if (event.keyCode === 13) {
            this.search(false);
        }
    }
    selectStart(position: Response) {
        this.layerEndpoint.showStartMarker(position.lat, position.lon);
    }
    selectEnd(position: Response) {
        this.layerEndpoint.showEndMarker(position.lat, position.lon);
    }
    search(isStart: boolean) {
        let text;
        if (isStart) {
            text = this.start;
        } else {
            text = this.end;
        }
        this.geocoderSearch(text).then(res => {
            if (res.length === 0) {
                this.notificationService.info('Geocoder info',
                    'Nothing not found, please change your request and try again');
            }
            if (isStart) {
                this.startPositions = res;
            } else {
                this.endPositions = res;
            }
        });
    }
    geocoderSearch(text: string): Promise<Response[]> {
        let params = '?q=' + text + '&limit=10&format=json&addressdetails=1&bounded=1'
            + '&viewbox=' + this.profile.southWestLon + ',' + this.profile.southWestLat
            + ',' + this.profile.northEastLon + ',' + this.profile.northEastLat;
        return this.http.get(this.GEOCODER_URL + params)
            .toPromise().then(res => res.json() as Response[])
            .catch(this.handleError);
    }
    private handleError(error: any): Promise<any> {
        console.error('An error occurred', error);
        this.notificationService.alert('Geocoder not working',
                'Geocoder temporarily not working, please use map for search');
        return Promise.reject(error.message || error);
    }
}

class Response {
    constructor(
        public lat: number,
        public lon: number,
        public display_name: string
    ) {}
}
