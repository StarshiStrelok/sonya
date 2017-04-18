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

import {Component, OnInit, ViewChild, ElementRef} from '@angular/core';
import {ActivatedRoute, Params} from '@angular/router';
import {Location} from '@angular/common';

import {DataService} from '../service/data.service';
import {DialogService} from '../service/dialog.service';
import {LeafletMap, CtxMenuItem} from './leaflet.map';

import {TransportProfile} from '../model/transport-profile';
import {ModelClass} from '../model/abs.model';
import {BusStop} from '../model/busstop';
import {BusStopForm} from './../form/bus-stop.form';

@Component({
    selector: 'transport-profile-map',
    templateUrl: './transport-profile.map.html',
    styleUrls: ['./transport-profile.map.css']
})
export class TransportProfileMap extends LeafletMap implements OnInit {
    @ViewChild('map') mapElement: ElementRef;
    private profileId: number;
    constructor(
        private location: Location,
        private dataService: DataService,
        private dialogService: DialogService,
        private activatedRoute: ActivatedRoute
    ) {super()}
    ngOnInit() {
        this.activatedRoute.params.subscribe((params: Params) => {
            let id = params['id'];
            if (id) {
                this.profileId = id;
                this.dataService.findById<TransportProfile>(
                    id, ModelClass.TRANSPORT_PROFILE).then((profile: TransportProfile) => {
                        this.createMap(profile, this.mapElement);
                        this.addControl('close', this.fnBack, this, 'Close map', 'bottomright');
                        this.createContextMenu(200, [
                            new CtxMenuItem('add_location', 'Add bus stop', this.fnAddBusStop, this)
                        ]);
                    });
            }
        });
    }
    fnBack = function (component: TransportProfileMap) {
        component.location.back();
    }
    
    fnAddBusStop = function (component: TransportProfileMap) {
        component.dialogService.openWindow('New bus stop', '', '50%', BusStopForm, {
            profileId: component.profileId,
            model: new BusStop(null, null, component.coords.lat, component.coords.lng, null)
        }).subscribe((res: boolean) => {
                console.log('window res [' + res + ']');
            });
    }
}
