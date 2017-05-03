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
    GEOCODER_STRAIGHT_URL: string = 'http://nominatim.openstreetmap.org/search/';
    GEOCODER_REVERSE_URL: string = 'http://nominatim.openstreetmap.org/reverse/';
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
        this.layerEndpoint.showStartMarker(position.lat, position.lon, true);
    }
    selectEnd(position: Response) {
        this.layerEndpoint.showEndMarker(position.lat, position.lon, true);
    }
    selectStartAlt(event: any) {
        let filter: Response[] = this.startPositions.filter(pos => pos.display_name === this.start);
        if (filter.length === 1) {
            let pos = filter[0];
            this.layerEndpoint.showStartMarker(pos.lat, pos.lon, true);
        }
    }
    selectEndAlt(event: any) {
        let filter: Response[] = this.endPositions.filter(pos => pos.display_name === this.end);
        if (filter.length === 1) {
            let pos = filter[0];
            this.layerEndpoint.showEndMarker(pos.lat, pos.lon, true);
        }
    }
    clearStart() {
        this.start = '';
        this.startPositions = [];
        this.layerEndpoint.hideStartMarker();
    }
    clearEnd() {
        this.end = '';
        this.endPositions = [];
        this.layerEndpoint.hideEndMarker();
    }
    search(isStart: boolean) {
        let text;
        if (isStart) {
            text = this.start;
        } else {
            text = this.end;
        }
        this.geocoderStraightSearch(text).then(res => {
            if (res.length === 0) {
                this.notificationService.info('Geocoder info',
                    'Nothing not found, please change your request and try again');
            } else if (res.length === 1) {
                if (isStart) {
                    this.startPositions = [];
                    this.start = res[0].display_name;
                    this.selectStart(res[0]);
                } else {
                    this.endPositions = [];
                    this.end = res[0].display_name;
                    this.selectEnd(res[0]);
                }
            } else {
                if (isStart) {
                    this.startPositions = res;
                } else {
                    this.endPositions = res;
                }
            }
        });
    }
    reverseSearch(isStart: boolean, lat: number, lon: number) {
        this.geocoderReverseSearch(lat, lon).then(res => {
            if (!res) {
                return;
            }
            if (isStart) {
                this.startPositions = [];
                this.start = res.display_name;
            } else {
                this.endPositions = [];
                this.end = res.display_name;
            }
        });
    }
    private geocoderStraightSearch(text: string): Promise<Response[]> {
        let params = '?q=' + text + '&limit=10&format=json&addressdetails=1&bounded=1'
            + '&viewbox=' + this.profile.southWestLon + ',' + this.profile.southWestLat
            + ',' + this.profile.northEastLon + ',' + this.profile.northEastLat;
        return this.http.get(this.GEOCODER_STRAIGHT_URL + params)
            .toPromise().then(res => res.json() as Response[])
            .catch(err => this.handleError(err));
    }
    private geocoderReverseSearch(lat: number, lon: number): Promise<Response> {
        let params = '?lat=' + lat + '&lon=' + lon + '&format=json&addressdetails=1';
        return this.http.get(this.GEOCODER_REVERSE_URL + params)
            .toPromise().then(res => res.json() as Response)
            .catch(err => this.handleError(err));
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
