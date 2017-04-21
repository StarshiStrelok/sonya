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
import {BusStop} from '../model/abs.model';
import {OSRMResponse} from '../model/osrm.response';
import {Http} from '@angular/http';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/toPromise';

@Injectable()
export class OSRMService {
    constructor(private http: Http) {}
    requestPath(way: BusStop[], url: string): Promise<OSRMResponse> {
        let coords = '';
        way.forEach(bs => coords += (bs.longitude + ',' + bs.latitude + ';'));
        coords = coords.substring(0, coords.length - 1);
        let params = '?';
        params += 'overview=false&geometries=geojson&steps=true';
        return this.http.get(url + '/driving/' + coords + params)
        .toPromise()
            .then(res => res.json() as OSRMResponse)
            .catch(this.handleError);
    }
    private handleError(error: any): Promise<any> {
        console.error('An error occurred', error); // for demo purposes only
        return Promise.reject(error.message || error);
    }
}
