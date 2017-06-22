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

import {Window, DialogContent} from '../component/window';
import {SecurityService} from '../service/security.service';

@Component({
    selector: 'registration-form',
    templateUrl: './registration.form.html',
})
export class RegistrationForm extends DialogContent implements OnInit {
    regForm: FormGroup;
    constructor(
        private fb: FormBuilder,
        private securityService: SecurityService
    ) {super()}
    ngOnInit() {
        this.createForm();
    }
    createForm() {
        this.regForm = this.fb.group({
            login: ['', [Validators.required, Validators.maxLength(100)]],
            password: ['', [Validators.required, Validators.maxLength(100)]]
        });
    }
    setData(data: any): void {
    }
    setDialogRef(dialogRef: MdDialogRef<Window>): void {
        this.dialogRef = dialogRef;
    }
    onSubmit() {
        if (!this.regForm.valid) {
            return;
        }
        let values = this.regForm.value;
        console.log(values);
        this.securityService.createProfile(values).then(res => {
            this.dialogRef.close(true);
        });
    }
}
