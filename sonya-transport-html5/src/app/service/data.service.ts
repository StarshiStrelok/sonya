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
import {AbsModel, Route, ModelClass, Path, ImportDataEvent,
    SearchSettings, OptimalPath, Trip} from '../model/abs.model';
import {Http, Headers} from '@angular/http';
import {NotificationsService} from 'angular2-notifications';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/toPromise';

import {Waiting} from '../lib/material/waiting';

@Injectable()
export class DataService {
    /** REST URL. */
    private dataUrl: string = '/rest/data/';
    private headers = new Headers({'Content-Type': 'application/json'});

    constructor(private http: Http, private notificationService: NotificationsService) {}
    create<T extends AbsModel>(model: T, clazz: string): Promise<T> {
        console.log('create model [' + clazz + '] start...');
        return this.http.post(
            this.dataUrl + clazz, JSON.stringify(model), {headers: this.headers}
        ).toPromise().then(res => res.json() as T).catch(err => this.handleErrorUI(err));
    }
    update<T extends AbsModel>(model: T, clazz: string): Promise<T> {
        console.log('update model [' + model['id'] + '] [' + clazz + '] start...');
        return this.http.put(
            this.dataUrl + clazz, JSON.stringify(model), {headers: this.headers}
        ).toPromise().then(res => res.json() as T).catch(err => this.handleErrorUI(err));
    }
    getAll<T extends AbsModel>(clazz: string): Promise<T[]> {
        //console.info('get all [' + clazz + '] start...');
        return this.http.get(
            this.dataUrl + clazz + '/all', {headers: this.headers}
        ).toPromise().then(res => res.json() as T[]).catch(err => this.handleErrorUI(err));
    }
    findById<T extends AbsModel>(id: number, clazz: string): Promise<T> {
        //console.log('get by id [' + id + '] [' + clazz + ']');
        return this.http.get(
            this.dataUrl + clazz + '/' + id, {headers: this.headers}
        ).toPromise().then(res => res.json() as T).catch(err => this.handleErrorUI(err));
    }
    deleteById<T extends AbsModel>(id: number, clazz: string): Promise<boolean> {
        console.log('delete [' + id + '] [' + clazz + ']');
        return this.http.delete(
            this.dataUrl + clazz + '/' + id, {headers: this.headers}
        ).toPromise().then(res => true).catch(err => this.handleErrorUI(err));
    }
    getFromProfile<T extends AbsModel>(id: number, clazz: string): Promise<T[]> {
        // console.log('get from profile [' + id + '] [' + clazz + ']');
        return this.http.get(
            this.dataUrl + clazz + '/from-profile/' + id, {headers: this.headers}
        ).toPromise().then(res => res.json() as T[]).catch(err => this.handleErrorUI(err));
    }
    getRoutesFromSameType(id: number): Promise<Route[]> {
        return this.http.get(
            this.dataUrl + ModelClass.ROUTE + '/from-type/' + id, {headers: this.headers}
        ).toPromise().then(res => res.json() as Route[]).catch(err => this.handleErrorUI(err));
    }
    getPathsFromRoute(id: number): Promise<Path[]> {
        return this.http.get(
            this.dataUrl + ModelClass.PATH + '/from-route/' + id, {headers: this.headers}
        ).toPromise().then(res => res.json() as Path[]).catch(err => this.handleErrorUI(err));
    }
    importData(tid: number, rid: number, file: any, persist: boolean): Promise<ImportDataEvent[]> {
        return this.http.post(
            this.dataUrl + 'import/' + tid + '/' + rid + '/' + persist, file, {}
        ).toPromise().then(res => res.json() as ImportDataEvent[])
            .catch(err => this.handleErrorUI(err));
    }
    searchRoutes(settings: SearchSettings, waiting: Waiting): Promise<OptimalPath[]> {
        //console.log(settings);
        return this.http.post(
            this.dataUrl + ModelClass.ROUTE + '/search', JSON.stringify(settings), {headers: this.headers}
        ).toPromise().then(res => res.json() as OptimalPath[]
        ).catch(err => this.handleSearchError(err, waiting));
    }
    uploadBusStopMarker(id: number, file: any): Promise<any> {
        return this.http.post(
            this.dataUrl + ModelClass.TRANSPORT_PROFILE + '/route/marker/' + id, file, {}
        ).toPromise().then(res => res)
            .catch(err => this.handleErrorUI(err));
    }
    getSchedule(id: number): Promise<Trip[]> {
        return this.http.get(
            this.dataUrl + ModelClass.PATH + '/schedule/' + id, {headers: this.headers}
        ).toPromise().then(res => res.json() as Trip[]).catch(err => this.handleErrorUI(err));
    }
    private handleErrorUI(error: any): Promise<any> {
        console.error('An error occurred', error);
        let status = error.status;
        if (status === 404 || status === 504) {
            this.notificationService.error('No connection',
                'Connection to the server is not available, please try later');
        } else if (status === 500) {
            this.notificationService.error('Server error',
                'Internal server error occured, please try again');
        } else if (status === 401) {
            this.notificationService.warn('Access denied',
                'Authentication required');
        } else {
            this.notificationService.error('Error',
                'Unknown error occured');
        }
        return Promise.reject(error.message || error);
    }
    private handleSearchError(error: any, waiting: Waiting): Promise<any> {
        waiting.close();
        console.error('An error occurred', error);
        let status = error.status;
        if (status === 404 || status === 504 || status === 0) {
            this.notificationService.error('No connection',
                'Connection to the server is not available, please try later');
        } else {
            this.notificationService.error('Search error',
                'Error occured during search, please try again');
        }
        return Promise.reject(error.message || error);
    }
}
