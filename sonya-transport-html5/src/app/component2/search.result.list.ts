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
import {trigger, state, style, transition, animate} from '@angular/animations'

import {OptimalPath, BusStop} from '../model/abs.model';
import {TransportMap} from '../component2/transport.map';

@Component({
    selector: 'search-result',
    templateUrl: './search.result.list.html',
    animations: [
        trigger('flyInOut', [
            state('in', style({transform: 'translateX(0)'})),
            transition('void => *', [
                style({transform: 'translateX(-100%)'}),
                animate(200)
            ]),
            transition('* => void', [
                animate(200, style({transform: 'translateX(100%)'}))
            ])
        ])
    ],
    styleUrls: ['./search.result.list.css']
})
export class SearchResultList {
    @Input() mapComponent: TransportMap;
    result: OptimalPath[] = [];
    activePath: OptimalPath;
    flags: any = {
        isDetailsOpen: false
    }
    detailsTrigger = '';
    setResult(res: OptimalPath[]) {
        var i = 0, l = res.length;
        this.result = [];
        var orig = this.result;
        let itr = function() {
            res[i]['animationTrigger'] = 'in';
            orig.push(res[i]);
            if (++i < l) {
                setTimeout(itr, 200);
            }
        };
        itr();
        //this.result = res;
        if (this.result.length === 0) {
            // TODO message
        } else {
            this.activePath = this.result[0];
            this.mapComponent.layerEndpoint.searchRouteCtrl.drawRoute(this.activePath);
        }
    }
    openDetails(path: OptimalPath, event: any) {
        this.activePath = path;
        this.flags.isDetailsOpen = true;
        this.mapComponent.layerEndpoint.searchRouteCtrl.drawRoute(path);
        this.detailsTrigger = 'in';
        event.stopPropagation();
    }
    closeDetails() {
        this.flags.isDetailsOpen = false;
        //this.activePath['animationTrigger'] = 'in';
        this.detailsTrigger = 'void';
    }
    detailsBusStop(index: number) {
        console.log('index: ' + index);
    }
    getBusStopTime(index: number, position: string) {
        if (!this.activePath.schedule) {
            return '';
        } else {
            if ('start' === position) {
                return this.convertMSToTime(this.activePath.schedule.data[index][0].time);
            } else if ('end' === position) {
                return this.convertMSToTime(this.activePath.schedule.data[index][1].time);
            }
        }
    }
    summaryDistance() {
        let dist = 0;
        this.activePath.path.forEach(p => {
            dist += Number(p['distance']);
        });
        return Number(dist).toFixed(1);
    }
    summaryTime() {
        if (!this.activePath.schedule) {
            return '?'
        } else {
            var d = new Date(this.activePath.schedule.duration);
            if (d.getHours() !== 0) {
                return this.convertMSToTime(this.activePath.schedule.duration);
            } else {
                return d.getMinutes() + ' min';
            }
        }
    }
    showPath(op: OptimalPath) {
        this.activePath = op;
        this.mapComponent.layerEndpoint.searchRouteCtrl.drawRoute(op);
    }
    flyToBusStop(bs: BusStop) {
        this.mapComponent.layerEndpoint.moveToMarker(bs.latitude, bs.longitude);
    }
    flyToWay(way: BusStop[]) {
        this.mapComponent.layerEndpoint.searchRouteCtrl.flyToBounds(way, 0);
    }
    private convertMSToTime(ms: number) {
        var d = new Date(ms);
        var t = (d.getHours() < 10 ? '0' + d.getHours() : d.getHours()) + ':'
        + (d.getMinutes() < 10 ? '0' + d.getMinutes() : d.getMinutes());
        return t;
    }
}
