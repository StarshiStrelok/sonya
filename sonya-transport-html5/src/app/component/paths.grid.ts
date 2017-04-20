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

import {TransportProfileMap, SwitchedContent} from './transport-profile.map';
import {DataService} from './../service/data.service';
import {DialogService} from './../service/dialog.service';
import {PathForm} from './../form/path.form';
import {RoutesGrid} from './../component/routes.grid';
import {ModelClass, Route, Path} from './../model/abs.model';

@Component({
    selector: 'paths-grid',
    templateUrl: './paths.grid.html',
    styles: [`.pg-button {width: 100%; text-align: left;}`]
})
export class PathsGrid implements OnInit, AfterViewInit, SwitchedContent {
    public profileId: number;
    public route: Route;
    public mapComponent: TransportProfileMap;
    paths: Path[];
    constructor(
        private dataService: DataService,
        private dialogService: DialogService
    ) {}
    ngOnInit() {}
    setData(data: any) {
        this.mapComponent = data.component;
        this.profileId = this.mapComponent.profileId;
        this.route = data.route;
    }
    ngAfterViewInit() {
        this.loadPaths();
    }
    loadPaths() {
        this.dataService.getPathsFromRoute(this.route.id)
            .then((paths: Path[]) => {this.paths = paths});
    }
    openCreatePathDialog() {
        this.dialogService.openWindow('New path', '', '50%', PathForm, {
            profileId: this.profileId,
            routeId: this.route.id,
            model: new Path(null, null, null)
        }).subscribe((res: boolean) => {
            if (res) {
                this.loadPaths();
            }
        });
    }
    goBack() {
        this.mapComponent.sideNavTmpl.viewContainerRef.clear();
        this.mapComponent.switchSideNavContent(RoutesGrid, {
            mapComponent: this.mapComponent
        })
    }
    editPath(path: Path) {
        delete path['route'];
        delete path['busstops'];
        this.dialogService.openWindow('Edit path', '', '50%', PathForm, {
            profileId: this.profileId,
            routeId: this.route.id,
            model: path
        }).subscribe((res: boolean) => {
            if (res) {
                this.loadPaths();
            }
        });
    }
    deletePath(path: Path) {
        this.dialogService.confirm(
            'Delete path',
            'Are you sure that you want delete this path?'
        ).subscribe((result) => {
            if (result) {
                this.dataService.deleteById(path.id, ModelClass.PATH)
                .then((result: boolean) => {
                    if (result) {
                        this.loadPaths();
                    }
                });
            }
        });
    }
}
