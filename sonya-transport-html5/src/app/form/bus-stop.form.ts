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

import {BusStop} from '../model/busstop';
import {ModelClass} from '../model/abs.model';
import {DataService} from '../service/data.service';
import {minNumberValidator} from '../lib/validator/min-number.directive';
import {maxNumberValidator} from '../lib/validator/max-number.directive';
import {Window} from '../component/window';

@Component({
    selector: 'bus-stop-form',
    templateUrl: './bus-stop.form.html',
})
export class BusStopForm implements OnInit {
    private dialogRef: MdDialogRef<Window>;
    private profileId: number;
    busStopForm: FormGroup;
    busStop: BusStop = new BusStop(null, null, null, null, null);
    constructor(
        private fb: FormBuilder,
        private dataService: DataService,
    ) {}
    ngOnInit() {
        if (this.busStop.id) {
            this.dataService.findById<BusStop>(
                this.busStop.id, ModelClass.BUS_STOP).then((busStop: BusStop) => {
                    this.busStop = busStop;
                    this.createForm();
                });
        } else {
            this.createForm();
        }
    }
    createForm() {
        this.busStopForm = this.fb.group({
            id: [''],
            name: ['', [Validators.required, Validators.maxLength(100)]],
            latitude: ['', [Validators.required, minNumberValidator(-90), maxNumberValidator(90)]],
            longitude: ['', [Validators.required, minNumberValidator(-180), maxNumberValidator(180)]],
            externalId: ['']
        });
        this.busStopForm.setValue(this.busStop);
    }
    onSubmit() {
        if (!this.busStopForm.valid) {
            return;
        }
        let values = this.busStopForm.value;
        Object.getOwnPropertyNames(values).map(
            (key: string) => {
                this.busStop[key] = values[key];
            }
        );
        this.busStop['transportProfile'] = {id: this.profileId};
        console.log(JSON.stringify(this.busStop));
        if (this.busStop.id) {
            this.dataService.update<BusStop>(this.busStop, ModelClass.BUS_STOP)
                .then((bs: BusStop) => {});
        } else {
            this.dataService.create<BusStop>(this.busStop, ModelClass.BUS_STOP)
                .then((bs: BusStop) => {});
        }
    }
    // ========================================================================
    setDialogRef(dialogRef: MdDialogRef<Window>): void {
        this.dialogRef = dialogRef;
    }
    setData(data: any): void {
        this.profileId = data.profileId;
        console.log('#bus stop form# profile id [' + this.profileId + ']');
        this.busStop = data.model;
    }
}
