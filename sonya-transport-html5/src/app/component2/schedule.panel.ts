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

import {Component, Input, Pipe, PipeTransform} from '@angular/core';

import {TransportMap} from './transport.map';
import {RouteProfile, Route, Path, Trip, BusStop} from '../model/abs.model';
import {DataService} from '../service/data.service';

@Component({
    selector: 'schedule-panel',
    templateUrl: './schedule.panel.html',
    styleUrls: ['./schedule.panel.css']
})
export class SchedulePanel {
    private ALL_BS_FILTER: BusStop = new BusStop(-1, '#Все остановки', null, null, null);
    @Input() parent: TransportMap;
    private selectedType: RouteProfile;
    private routes: Route[];
    private selectedRoute: Route;
    private paths: Path[];
    private selectedPath: Path;
    private schedule: any;
    private scheduleDays: any = [];
    private isIrregular = false;
    filterByBusStop: BusStop = this.ALL_BS_FILTER;
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
            this.dataService.getSchedule(this.selectedPath.id).then((res: Trip[]) => {
                if (res && res.length > 0) {
                    if (res[0].regular) {
                        this.regularSchedule(res);
                    } else if (res[0].irregular) {
                        this.irregularSchedule(res);
                    }
                }
            });
        }
    }
    private irregularSchedule(res: Trip[]) {
        this.filterByBusStop = this.ALL_BS_FILTER;
        let daysMap: any = {};
        res.forEach(trip => {
            let timeArray: string[];
            if (daysMap[trip.days]) {
                timeArray = daysMap[trip.days];
            } else {
                timeArray = new Array();
                daysMap[trip.days] = timeArray;
            }
            timeArray.push(trip.irregular);
        });
        let tableData: any = {};
        let daysData = new Array();
        for (let days in daysMap) {
            daysData.push(days);
            let sb = this.selectedPath.busstops[0].name + ': ';
            let irr = daysMap[days];
            let irrO = JSON.parse(irr);
            let times = irrO.time;
            let intervals = irrO.interval;
            if (times && times.length > 0) {
                for (let i = 0; i < times.length; i++) {
                    sb += times[i];
                    if (i + 1 < times.length) {
                        sb += ', ';
                    }
                }
                sb += '. '
            }
            if (intervals && intervals.length > 0) {
                sb += '#Интервалы движения: ';
                for (let i = 0; i < intervals.length; i++) {
                    let interval = intervals[i].replace("=", " #интервал ");
                    interval = interval.replace("-", " - ");
                    interval += " #мин";
                    sb += interval;
                    if (i + 1 < interval.length) {
                        sb += ', ';
                    }
                }
            }
            tableData[days] = sb;
        }
        this.schedule = tableData;
        this.scheduleDays = daysData;
        this.isIrregular = true;
    }
    private regularSchedule(res: Trip[]) {
        this.isIrregular = false;
        let way: BusStop[] = this.selectedPath.busstops;
        let daysMap: any = {};
        res.forEach(trip => {
            let timeArray: string[][];
            if (daysMap[trip.days]) {
                timeArray = daysMap[trip.days];
            } else {
                timeArray = new Array();
                daysMap[trip.days] = timeArray;
            }
            timeArray.push(trip.regular.split(','));
        });
        let tableData: any = {};
        let daysData = new Array();
        for (let days in daysMap) {
            daysData.push(days);
            let dayTrips = daysMap[days];
            let table = new Array();
            for (let i = 0; i < way.length; i++) {
                let row = new Array();
                row.push(way[i].name);
                for (let j = 0; j < dayTrips.length; j++) {
                    row.push(dayTrips[j][i]);
                }
                table.push(row);
            }
            tableData[days] = table;
        }
        this.schedule = tableData;
        this.scheduleDays = daysData;
    }
    convertDays(days: string) {
        if ('1,7' === days) {
            return '#вых';
        } else if ('2,3,4,5,6' === days) {
            return '#раб';
        } else if ('1,2,3,4,5,6,7' === days) {
            return '#еж'
        } else {
            let sb = '';
            let dayArr: string[] = days.split(',');
            for (let i = 0; i < dayArr.length; i++) {
                if (dayArr[i] === '1') {
                    sb += '#пн';
                } else if (dayArr[i] === '2') {
                    sb += '#пн';
                } else if (dayArr[i] === '3') {
                    sb += '#пн';
                } else if (dayArr[i] === '4') {
                    sb += '#пн';
                } else if (dayArr[i] === '5') {
                    sb += '#пн';
                } else if (dayArr[i] === '6') {
                    sb += '#пн';
                } else if (dayArr[i] === '7') {
                    sb += '#пн';
                }
                if (i + 1 < dayArr.length) {
                    sb += ',';
                }
            }
            return sb
        }
    }
    filterBusStopsArray() {
        let result: BusStop[] = [];
        result.push(this.ALL_BS_FILTER);
        if (this.selectedPath && this.selectedPath.busstops) {
            this.selectedPath.busstops.forEach(bs => {
                result.push(bs);
            });
        }
        return result;
    }
    clearData(full: boolean) {
        this.scheduleDays = [];
        this.schedule = [];
        this.filterByBusStop = this.ALL_BS_FILTER;
        this.selectedPath = null;
        if (full) {
            this.selectedType = null;
            this.selectedRoute = null;
        }
    }
}

@Pipe({
    name: 'filterBs',
    pure: false
})
export class FilterByBusStopPipe implements PipeTransform {
    transform(items: string[], filter: BusStop): any {
        if (!items || !filter || filter.id === -1) {
            return items;
        }
        return items.filter(item => item[0] === filter.name);
    }
}

