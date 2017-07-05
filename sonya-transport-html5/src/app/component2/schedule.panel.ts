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

import {Component, Input, Pipe, PipeTransform, OnInit} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';

import {TransportMap} from './transport.map';
import {RouteProfile, Route, Path, Trip, BusStop} from '../model/abs.model';
import {DataService} from '../service/data.service';

@Component({
    selector: 'schedule-panel',
    templateUrl: './schedule.panel.html',
    styleUrls: ['./schedule.panel.css']
})
export class SchedulePanel implements OnInit {
    @Input() parent: TransportMap;
    selectedType: RouteProfile;
    private routes: Route[];
    selectedRoute: Route;
    private paths: Path[];
    selectedPath: Path;
    private schedule: any;
    private scheduleDays: any = [];
    private isIrregular = false;
    filterByBusStop: BusStop;
    constructor(
        private dataService: DataService,
        private translate: TranslateService
    ) {}
    ngOnInit() {

    }
    typeChanged() {
        this.selectedRoute = null;
        this.selectedPath = null;
        if (this.selectedType) {
            this.selectedType['formattedDate'] = this.formatDate(this.selectedType.lastUpdate);
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
                sb += this.translate.instant('transport-map.schedule.interval') + ": ";
                let intStr = this.translate.instant('transport-map.schedule.interval2');
                let minStr = this.translate.instant('common.time.min');
                for (let i = 0; i < intervals.length; i++) {
                    let interval = intervals[i].replace("=", " " + intStr + " ");
                    interval = interval.replace("-", " - ");
                    interval += " " + minStr;
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
            return this.translate.instant('common.day-short.weekend');
        } else if ('2,3,4,5,6' === days) {
            return this.translate.instant('common.day-short.weekdays');
        } else if ('1,2,3,4,5,6,7' === days) {
            return this.translate.instant('common.day-short.everydays');
        } else {
            let sb = '';
            let dayArr: string[] = days.split(',');
            for (let i = 0; i < dayArr.length; i++) {
                if (dayArr[i] === '1') {
                    sb += this.translate.instant('common.day-short.sunday');
                } else if (dayArr[i] === '2') {
                    sb += this.translate.instant('common.day-short.monday');
                } else if (dayArr[i] === '3') {
                    sb += this.translate.instant('common.day-short.tuesday');
                } else if (dayArr[i] === '4') {
                    sb += this.translate.instant('common.day-short.wednesday');
                } else if (dayArr[i] === '5') {
                    sb += this.translate.instant('common.day-short.thursday');
                } else if (dayArr[i] === '6') {
                    sb += this.translate.instant('common.day-short.friday');
                } else if (dayArr[i] === '7') {
                    sb += this.translate.instant('common.day-short.saturday');
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
        this.selectedPath = null;
        if (full) {
            this.selectedType = null;
            this.selectedRoute = null;
        }
    }
    private formatDate(date: Date) {
        date = new Date(date);
        if (date) {
            let dd: any = date.getDate();
            let mm: any = date.getMonth() + 1;
            let yyyy = date.getFullYear();
            if (dd < 10) {
                dd = '0' + dd;
            }
            if (mm < 10) {
                mm = '0' + mm;
            }
            return dd + '.' + mm + '.' + yyyy;
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

