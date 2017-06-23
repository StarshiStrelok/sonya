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
import {Http, Headers} from '@angular/http';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/toPromise';

@Injectable()
export class SecurityService {
    private url: string = '/rest/transport/security';
    private headers = new Headers({'Content-Type': 'application/json'});
    constructor(private http: Http) {}
    profilesCount(): Promise<string> {
        return this.http.get(this.url + '/profiles-count')
            .toPromise()
            .then(res => res.text() as string)
            .catch(this.handleError);
    }
    createProfile(data: any): Promise<string> {
        return this.http.post(this.url + '/new-account', JSON.stringify(data), {headers: this.headers})
            .toPromise()
            .then(res => res.text() as string)
            .catch(this.handleError);
    }
    authentication(credentials: any): Promise<number> {
        let token = credentials ? {
            authorization: "Basic " + btoa(credentials.login
                + ":" + credentials.password)
        } : {};
        let authheaders = new Headers(token);
        return this.http.get(this.url + '/authentication', {headers: authheaders})
            .toPromise()
            .then(res => res.status as number)
            .catch(this.handleError);
    }
    private handleError(error: any): Promise<any> {
        console.error('An error occurred', error); // for demo purposes only
        return Promise.reject(error.message || error);
    }
}
