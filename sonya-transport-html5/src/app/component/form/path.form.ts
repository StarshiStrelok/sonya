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

import {ModelClass, Route, RouteProfile, TransportProfile, Path} from '../../model/abs.model';
import {DataService} from '../../service/data.service';
import {Window, DialogContent} from '../window';

@Component({
    selector: 'path-form',
    templateUrl: './path.form.html',
})
export class PathForm extends DialogContent implements OnInit {
    private profileId: number;
    private routeId: number;
    pathForm: FormGroup;
    path: Path = new Path(null, null, null, null, null, null);
    constructor(
        private fb: FormBuilder,
        private dataService: DataService,
    ) {super()}
    ngOnInit() {
        this.createForm();
    }
    createForm() {
        this.pathForm = this.fb.group({
            id: [''],
            description: ['', [Validators.required, Validators.maxLength(100)]],
            externalId: ['']
        });
        this.pathForm.patchValue(this.path);
    }
    onSubmit() {
        if (!this.pathForm.valid) {
            return;
        }
        let values = this.pathForm.value;
        Object.getOwnPropertyNames(values).map(
            (key: string) => {
                this.path[key] = values[key];
            }
        );
        this.path.transportProfile = new TransportProfile(this.profileId, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, false);
        this.path.route = new Route(this.routeId, null, null, null, null);
        console.log(JSON.stringify(this.path));
        if (this.path.id) {
            this.dataService.update<Path>(this.path, ModelClass.PATH)
                .then(() => {
                    this.dialogRef.close(true);
                });
        } else {
            this.dataService.create<Path>(this.path, ModelClass.PATH)
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
        this.routeId = data.routeId;
        this.path = data.model;
    }
}
