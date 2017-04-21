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
import {PathsGrid} from './../component/paths.grid';
import {ModelClass, Route, Path, BusStop} from './../model/abs.model';

@Component({
    selector: 'busstop-grid',
    templateUrl: './busstop.grid.html',
    styles: [`.bsg-btn {text-align: left}`]
})
export class BusStopGrid implements OnInit, AfterViewInit, SwitchedContent {
    public profileId: number;
    public path: Path;
    public mapComponent: TransportProfileMap;
    busstops: BusStop[];
    constructor(
        private dataService: DataService,
        private dialogService: DialogService
    ) {}
    ngOnInit() {}
    setData(data: any) {
        this.mapComponent = data.component;
        this.profileId = this.mapComponent.profileId;
        this.path = data.path;
        console.log(this.path);
    }
    ngAfterViewInit() {
        this.loadBusStops();
    }
    loadBusStops() {
        this.dataService.findById<Path>(this.path.id, ModelClass.PATH)
            .then((path: Path) => {
                this.busstops = path.busstops;
                this.path = path;
            });
    }
    goBack() {
        this.mapComponent.sideNavTmpl.viewContainerRef.clear();
        this.mapComponent.switchSideNavContent(PathsGrid, {
            component: this.mapComponent,
            route: this.path.route
        })
        this.mapComponent.layerRouting.clearLayers();
    }
    deleteBusStop(targetBs: BusStop) {
        this.busstops = this.busstops.filter(bs => bs.id != targetBs.id);
        this.mapComponent.drawRoute(this.busstops, this.path.route.type);
    }
    moveBusStopUp(bs: BusStop) {
        var index = this.busstops.indexOf(bs);
        var nextIndex = (index === 0 ? 0 : (index - 1));
        this.moveBusStop(index, nextIndex);
        this.mapComponent.drawRoute(this.busstops, this.path.route.type);
    }
    moveBusStopDown(bs: BusStop) {
        var index = this.busstops.indexOf(bs);
        var nextIndex = (index === (this.busstops.length - 1) ? index : (index + 1));
        this.moveBusStop(index, nextIndex);
        this.mapComponent.drawRoute(this.busstops, this.path.route.type);
    }
    private moveBusStop(fromIndex: number, toIndex: number) {
        var element = this.busstops[fromIndex];
        this.busstops.splice(fromIndex, 1);
        this.busstops.splice(toIndex, 0, element);
    }
}
