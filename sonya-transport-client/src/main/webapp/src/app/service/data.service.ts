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
import {AbsModel} from '../model/abs.model';
import {Http, Headers} from '@angular/http';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/toPromise';

@Injectable()
export class DataService {
    /** REST URL. */
    private dataUrl: string = '/rest/data/';
    private headers = new Headers({'Content-Type': 'application/json'});
    constructor(private http: Http) {}
    create<T extends AbsModel>(model: T, clazz: string): Promise<T> {
        console.log('create model [' + clazz + '] start...');
        return this.http.post(
            this.dataUrl + clazz, JSON.stringify(model), {headers: this.headers}
        ).toPromise()
            .then(res => res.json() as T)
            .catch(this.handleError);
    }
    update<T extends AbsModel>(model: T, clazz: string): Promise<T> {
        console.log('update model [' + model['id'] + '] [' + clazz + '] start...');
        return this.http.put(
            this.dataUrl + clazz, JSON.stringify(model), {headers: this.headers}
        ).toPromise()
            .then(res => res.json() as T)
            .catch(this.handleError);
    }
    getAll<T extends AbsModel>(clazz: string): Promise<T[]> {
        console.info('get all [' + clazz + '] start...');
        return this.http.get(
            this.dataUrl + clazz + '/all', {headers: this.headers}
        ).toPromise()
            .then(res => res.json() as T[])
            .catch(this.handleError);
    }
    findById<T extends AbsModel>(id: number, clazz: string): Promise<T> {
        console.log('get by id [' + id + '] [' + clazz + ']');
        return this.http.get(
            this.dataUrl + clazz + '/' + id, {headers: this.headers}
        ).toPromise()
            .then(res => res.json() as T)
            .catch(this.handleError);
    }
    deleteById<T extends AbsModel>(id: number, clazz: string): Promise<boolean> {
        console.log('delete [' + id + '] [' + clazz + ']');
        return this.http.delete(
            this.dataUrl + clazz + '/' + id, {headers: this.headers}
        ).toPromise()
            .then(res => true)
            .catch(this.handleError);
    }
    private handleError(error: any): Promise<any> {
        console.error('An error occurred', error); // for demo purposes only
        return Promise.reject(error.message || error);
    }
}
