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

import {Component, OnInit, Input} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {MdDialogRef} from '@angular/material';

import {ModelClass, Route, RouteProfile, TransportProfile} from '../model/abs.model';
import {DataService} from '../service/data.service';
import {Window, DialogContent} from '../component/window';

@Component({
    selector: 'route-form',
    templateUrl: './route.form.html',
})
export class RouteForm extends DialogContent implements OnInit {
    private profileId: number;
    routeProfiles: RouteProfile[] = [];
    routeForm: FormGroup;
    route: Route = new Route(null, null, null, null, null);
    constructor(
        private fb: FormBuilder,
        private dataService: DataService,
    ) {super()}
    ngOnInit() {
        this.loadRouteProfiles();
        this.createForm();
    }
    loadRouteProfiles() {
        this.dataService.findById<TransportProfile>(this.profileId, ModelClass.TRANSPORT_PROFILE)
            .then((t: TransportProfile) => {
                this.routeProfiles = t.routeProfiles;
            });
    }
    createForm() {
        this.routeForm = this.fb.group({
            id: [''],
            type: ['', [Validators.required]],
            namePrefix: ['', [Validators.required, Validators.maxLength(10)]],
            namePostfix: ['', [Validators.maxLength(10)]],
            externalId: ['']
        });
        this.routeForm.setValue(this.route);
    }
    onSubmit() {
        if (!this.routeForm.valid) {
            return;
        }
        let values = this.routeForm.value;
        Object.getOwnPropertyNames(values).map(
            (key: string) => {
                this.route[key] = values[key];
            }
        );
        this.route['transportProfile'] = {id: this.profileId};
        console.log(JSON.stringify(this.route));
        if (this.route.id) {
            this.dataService.update<Route>(this.route, ModelClass.ROUTE)
                .then(() => {
                    this.dialogRef.close(true);
                });
        } else {
            this.dataService.create<Route>(this.route, ModelClass.ROUTE)
                .then(() => {
                    this.dialogRef.close(true);
                });
        }
    }
    // ========================================================================
    setDialogRef(dialogRef: MdDialogRef<Window>): void {
        this.dialogRef = dialogRef;
    }
    setData(data: any): void {
        this.profileId = data.profileId;
        console.log('#route form# profile id [' + this.profileId + ']');
        this.route = data.model;
    }
}
