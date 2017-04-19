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

import {Component, OnInit, AfterViewInit} from '@angular/core';

import {TransportProfileMap} from './transport-profile.map';
import {DataService} from './../service/data.service';
import {ModelClass, TransportProfile, RouteProfile, Route} from './../model/abs.model';

@Component({
    selector: 'routes-grid',
    templateUrl: './routes.grid.html'
})
export class RoutesGrid implements OnInit, AfterViewInit {
    public profileId: number;
    public mapComponent: TransportProfileMap;
    private routeProfiles: RouteProfile[] = [];
    selectedType: RouteProfile;
    routes: Route[] = [];
    constructor(
        private dataService: DataService
    ) {}
    ngOnInit() {
        
    }
    ngAfterViewInit() {
        this.dataService.findById<TransportProfile>(this.profileId, ModelClass.TRANSPORT_PROFILE)
            .then((profile: TransportProfile) => {
                this.routeProfiles = profile.routeProfiles;
            });
    }
    typeChanged() {
        if (this.selectedType) {
            this.dataService.getRoutesFromSameType(this.selectedType.id)
                .then((routes: Route[]) => {
                    this.routes = routes;
                });
        }
    }
}
