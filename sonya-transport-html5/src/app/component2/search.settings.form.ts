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

import {SearchSettings} from '../model/abs.model';
import {minNumberValidator} from '../lib/validator/min-number.directive';
import {maxNumberValidator} from '../lib/validator/max-number.directive';
import {TransportMap} from './transport.map';

@Component({
    selector: 'search-settings',
    templateUrl: './search.settings.form.html',
    styles: [`.ssf-spacer {
        height: 10px;
    }`]
})
export class SearchSettingsForm implements OnInit {
    settingsForm: FormGroup;
    settings: SearchSettings = new SearchSettings(
        null, null, null, null, null, null, null, 10, 3);
    @Input() mapComponent: TransportMap;
    constructor(
        private fb: FormBuilder
    ) {}
    ngOnInit() {
        this.createForm();
    }
    createForm() {
        this.settingsForm = this.fb.group({
            maxResults: ['', [Validators.required, minNumberValidator(0), maxNumberValidator(20)]],
            maxTransfers: ['', [Validators.required, minNumberValidator(0)]],
            day: ['', [Validators.required]],
            time: ['', [Validators.required, Validators.pattern('^([01]?[0-9]|2[0-3]):[0-5][0-9]$')]]
        });
        let now = new Date();
        let curDay = now.getDay() + 1;
        this.settings.day = curDay;
        let minutes = now.getMinutes() + '';
        minutes = minutes.length === 1 ? '0' + minutes : minutes;
        this.settings.time = now.getHours() + ':' + minutes;
        this.settingsForm.patchValue(this.settings);
    }
    onSubmit() {
        if (!this.settingsForm.valid) {
            console.log('invalid');
            return;
        } else {
            this.mapComponent.layerEndpoint.checkSearchConditions();
        }
    }
    getSettingsValues(): any {
        return this.settingsForm.value;
    }
}
