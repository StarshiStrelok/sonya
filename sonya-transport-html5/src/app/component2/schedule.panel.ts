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

import {Component, Input} from '@angular/core';

import {TransportMap} from './transport.map';
import {RouteProfile, Route, Path} from '../model/abs.model';
import {DataService} from '../service/data.service';

@Component({
    selector: 'schedule-panel',
    templateUrl: './schedule.panel.html',
    styles: [`
            .schedule-panel {
                margin: 10px;
                padding: 10px;
            }
            `]
})
export class SchedulePanel {
    @Input() parent: TransportMap;
    private selectedType: RouteProfile;
    private routes: Route[];
    private selectedRoute: Route;
    private paths: Path[];
    private selectedPath: Path;
    constructor(
        private dataService: DataService
    ) {}
    typeChanged() {
        this.selectedRoute = null;
        this.selectedPath = null;
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
    routeChanged() {
        this.selectedPath = null;
        if (this.selectedRoute) {
            this.dataService.getPathsFromRoute(this.selectedRoute.id)
                .then((paths: Path[]) => {this.paths = paths});
        }
    }
    pathChanged() {
        if (this.selectedPath) {
            console.log("PATH SELECTED");
        }
    }
}
