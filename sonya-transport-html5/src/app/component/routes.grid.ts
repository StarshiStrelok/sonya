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
import {NotificationsService} from 'angular2-notifications';

import {TransportProfileMap, SwitchedContent} from './transport-profile.map';
import {DataService} from './../service/data.service';
import {DialogService} from './../service/dialog.service';
import {RouteForm} from './form/route.form';
import {PathsGrid} from './paths.grid';
import {ConfirmImport} from './confirm.import.dialog';
import {ModelClass, TransportProfile, RouteProfile, Route, ImportDataEvent} from './../model/abs.model';
import {slideAnimation} from './../app.component';

@Component({
    selector: 'routes-grid',
    templateUrl: './routes.grid.html',
    animations: [slideAnimation]
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
        private dialogService: DialogService,
        private notificationService: NotificationsService
    ) {}
    setData(data: any) {
        this.mapComponent = data.mapComponent;
        this.profileId = this.mapComponent.profileId;
        this.selectedType = data.selectedType;
    }
    ngOnInit() {

    }
    ngAfterViewInit() {
        this.dataService.findById<TransportProfile>(this.profileId, ModelClass.TRANSPORT_PROFILE)
            .then((profile: TransportProfile) => {
                this.routeProfiles = profile.routeProfiles;
                if (this.routeProfiles.length > 0) {
                    if (!this.selectedType) {
                        this.selectedType = this.routeProfiles[0];
                    } else {
                        this.selectedType = this.routeProfiles.filter(p => p.id === this.selectedType.id)[0];
                    }
                    let _grid = this;
                    setTimeout(function () {        // performance
                        _grid.typeChanged();
                    }, 400);
                }
            });
    }
    typeChanged() {
        if (this.selectedType) {
            this.dataService.getRoutesFromSameType(this.selectedType.id)
                .then((routes: Route[]) => {
                    routes.sort(function (a, b) {
                        if (!isNaN(a.namePrefix as any) && !isNaN(b.namePrefix as any)) {
                            let dif = parseFloat(a.namePrefix) - parseFloat(b.namePrefix);
                            if (dif === 0) {
                                return (a.namePostfix === null ? '' : a.namePostfix)
                                        .localeCompare(b.namePostfix);
                            } else {
                                return dif;
                            }
                        } else {
                            return (a.namePrefix + a.namePostfix)
                                .localeCompare(b.namePrefix + b.namePostfix);
                        }
                    });
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
        if (!files[0]) {
            return;
        }
        binData.append('file', files[0]);
        this.mapComponent.showProgress = true;
        let inputFile: any = document.getElementById("import-data-upload");
        inputFile.value = "";
        this.dataService.importData(this.profileId, this.selectedType.id, binData, false)
            .then((events: ImportDataEvent[]) => {
                this.mapComponent.showProgress = false;
                if (events.length === 0) {
                    this.notificationService.info('Import details',
                        'Differences from current version not found');
                } else {
                    this.dialogService.openWindow('Confirm import', '', '50%', ConfirmImport, {
                        events: events,
                        persist: false
                    }).subscribe((res: boolean) => {
                        if (res) {
                            this.mapComponent.showProgress = true;
                            this.dataService.importData(this.profileId, this.selectedType.id, binData, true)
                                .then((events: ImportDataEvent[]) => {
                                    this.mapComponent.showProgress = false;
                                    this.dialogService.openWindow('Import completed', '', '50%', ConfirmImport, {
                                        events: events,
                                        persist: true
                                    }).subscribe((res: boolean) => {
                                        this.typeChanged();
                                    });
                                });
                        }
                    });
                }
            });
    }
}
