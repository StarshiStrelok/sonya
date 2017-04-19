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

import {TransportProfile, BusStop, ModelClass, Route} from '../model/abs.model';
import {BusStopForm} from './../form/bus-stop.form';
import {RouteForm} from './../form/route.form';

declare var L: any;

@Component({
    selector: 'transport-profile-map',
    templateUrl: './transport-profile.map.html',
    styleUrls: ['./transport-profile.map.css']
})
export class TransportProfileMap extends LeafletMap implements OnInit {
    @ViewChild('map') mapElement: ElementRef;
    private profileId: number;
    private ctxMenuMarker: any;
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
                        this.addControl('add_circle_outline', this.fnOpenCreateRouteDialog, this, 'New route', 'topright');
                        
                        this.ctxMenu = this.createContextMenu(180, [
                            new CtxMenuItem('add_circle_outline', 'Add bus stop', this.fnOpenCreateBusStopDialog, this)
                        ]);
                        this.ctxMenuMarker = this.createContextMenu(180, [
                            new CtxMenuItem('mode_edit', 'Edit bus stop', this.fnOpenEditBusStopDialog, this),
                            new CtxMenuItem('delete_forever', 'Delete bus stop', this.fnDeleteBusStop, this)
                        ]);
                        this.loadBusStops();
                    });
            }
        });
    }
    loadBusStops(): void {
        this.dataService.getFromProfile<BusStop>(this.profileId, ModelClass.BUS_STOP)
            .then((all: BusStop[]) => {
                console.log('profile bus stops loaded [' + all.length + ']');
                this.updateBusStopLayer(all);
            });
    }
    moveBusStop(model: BusStop, lat: number, lon: number) {
        this.dialogService.confirm('Move bus stop', 'Are you sure that you want replace this bus stop?')
            .subscribe((res: boolean) => {
                if (res) {
                    model.latitude = lat;
                    model.longitude = lon;
                    model['transportProfile'] = {id: this.profileId}
                    this.dataService.update<BusStop>(model, ModelClass.BUS_STOP)
                        .then(() => {
                            this.loadBusStops();
                        });
                } else {
                    this.loadBusStops();
                }
            });
    }
    fnBack = function (component: TransportProfileMap) {
        component.location.back();
    }

    fnOpenCreateBusStopDialog = function (component: TransportProfileMap) {
        component.dialogService.openWindow('New bus stop', '', '50%', BusStopForm, {
            profileId: component.profileId,
            model: new BusStop(null, null, component.coords.lat, component.coords.lng, null)
        }).subscribe((res: boolean) => {
            if (res) {
                component.loadBusStops();
            }
        });
    }
    fnOpenEditBusStopDialog = function (component: TransportProfileMap) {
        let model: BusStop = component.ctxMenuMarker.curMarker.info;
        component.dialogService.openWindow('New bus stop', '', '50%', BusStopForm, {
            profileId: component.profileId,
            model: model
        }).subscribe((res: boolean) => {
            if (res) {
                component.loadBusStops();
            }
        });
    }
    fnDeleteBusStop = function (component: TransportProfileMap) {
        let model: BusStop = component.ctxMenuMarker.curMarker.info;
        component.dataService.deleteById<BusStop>(model.id, ModelClass.BUS_STOP)
            .then(() => {
                component.loadBusStops();
            });
    }
    fnOpenCreateRouteDialog = function (component: TransportProfileMap) {
        component.dialogService.openWindow('New route', '', '50%', RouteForm, {
            profileId: component.profileId,
            model: new Route(null, null, null, null, null)
        }).subscribe((res: boolean) => {
            if (res) {
                console.log('route created');
            }
        });
    }
    // ================================ LEAFLET ===============================
    createMarker(bs: BusStop): any {
        var marker = L.marker(new L.LatLng(bs.latitude, bs.longitude), {
            icon: bs.name === this.MOCK_BS
                ? this.createIcon('busstop_mock') : this.createIcon('busstop'),
            clickable: true,
            draggable: true,
            title: bs.name
        });
        marker.info = bs;
        var _comp = this;
        marker.on('contextmenu', function (e: any) {
            _comp.coords = e.latlng;
            _comp.ctxMenuMarker.setLatLng(e.latlng);
            _comp.ctxMenuMarker.openOn(_comp.map);
            _comp.ctxMenuMarker.curMarker = e.target;
        });
        marker.on('dragend', function (e: any) {
            var selMarker = e.target;
            var model = selMarker.info
            _comp.moveBusStop(model, selMarker.getLatLng().lat, selMarker.getLatLng().lng);
        });
        return marker;
    }
}
