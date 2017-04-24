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

import {Component, OnInit, AfterViewInit, ViewChild} from '@angular/core';
import {MdMenuTrigger} from '@angular/material';

import {TransportProfileMap, SwitchedContent} from './transport-profile.map';
import {DataService} from './../service/data.service';
import {DialogService} from './../service/dialog.service';
import {RouteForm} from './../form/route.form';
import {PathsGrid} from './paths.grid';
import {ConfirmImport} from './confirm.import.dialog';
import {ModelClass, TransportProfile, RouteProfile, Route} from './../model/abs.model';

@Component({
    selector: 'routes-grid',
    templateUrl: './routes.grid.html'
})
export class RoutesGrid implements OnInit, AfterViewInit, SwitchedContent {
    @ViewChild(MdMenuTrigger) ctxMenuTrigger: MdMenuTrigger;
    public profileId: number;
    public mapComponent: TransportProfileMap;
    private routeProfiles: RouteProfile[] = [];
    selectedType: RouteProfile;
    routes: Route[] = [];
    selectedRoute: Route;
    constructor(
        private dataService: DataService,
        private dialogService: DialogService
    ) {}
    setData(data: any) {
        this.mapComponent = data.mapComponent;
        this.profileId = this.mapComponent.profileId;
    }
    ngOnInit() {

    }
    ngAfterViewInit() {
        this.dataService.findById<TransportProfile>(this.profileId, ModelClass.TRANSPORT_PROFILE)
            .then((profile: TransportProfile) => {
                this.routeProfiles = profile.routeProfiles;
                if (this.routeProfiles.length > 0) {
                    this.selectedType = this.routeProfiles[0];
                    this.typeChanged();
                }
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
    openCreateRouteDialog() {
        this.dialogService.openWindow('New route', '', '50%', RouteForm, {
            profileId: this.profileId,
            model: new Route(null, null, null, null, null)
        }).subscribe((res: boolean) => {
            if (res) {
                this.typeChanged();
            }
        });
    }
    openPathsGrid(route: Route) {
        this.mapComponent.sideNavTmpl.viewContainerRef.clear();
        this.mapComponent.switchSideNavContent(PathsGrid, {
            component: this.mapComponent,
            route: route
        })
    }
    openCtxMenu(route: Route, event: any) {
        this.selectedRoute = route;
        event.stopPropagation();
        this.ctxMenuTrigger.toggleMenu();
        let menu: any = document.getElementsByClassName('cdk-overlay-pane')[0];
        menu.style.left = event.x + 'px';
        menu.style.top = event.y + 'px';
        return false;
    }
    editRoute() {
        this.dialogService.openWindow('Edit route', '', '50%', RouteForm, {
            profileId: this.profileId,
            model: this.selectedRoute
        }).subscribe((res: boolean) => {
            if (res) {
                this.typeChanged();
            }
        });
    }
    deleteRoute() {
        this.dialogService.confirm(
            'Delete route',
            'Are you sure that you want delete this route?'
        ).subscribe((result) => {
            if (result) {
                this.dataService.deleteById<Route>(this.selectedRoute.id, ModelClass.ROUTE)
                    .then((res: boolean) => {
                        this.typeChanged();
                    });
            }
        });
    }
    importData() {
        document.getElementById('import-data-upload').click();
    }
    selectFile(event: any) {
        let files = event.target.files;
        var binData = new FormData();
        binData.append('file', files[0]);
        this.dataService.importData(this.profileId, this.selectedType.id, binData, false)
            .then(events => {
                this.dialogService.openWindow('Confirm import', '', '50%', ConfirmImport, {
                    events: events,
                    persist: false
                }).subscribe((res: boolean) => {
                    if (res) {
                        this.dataService.importData(this.profileId, this.selectedType.id, binData, true)
                            .then(events => {
                                this.dialogService.openWindow('Import completed', '', '50%', ConfirmImport, {
                                    events: events,
                                    persist: true
                                }).subscribe((res: boolean) => {});
                            });
                    }
                });
            });
    }
}
