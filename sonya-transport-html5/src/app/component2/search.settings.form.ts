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

import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';

import {SearchSettings} from '../model/abs.model';
import {minNumberValidator} from '../lib/validator/min-number.directive';
import {maxNumberValidator} from '../lib/validator/max-number.directive';

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
    daysOfWeek: any[] = [];
    constructor(
        private fb: FormBuilder,
        private translate: TranslateService
    ) {}
    ngOnInit() {
        this.initDayOfWeek();
        this.createForm();
        console.log('search settings initialization complete...');
    }
    createForm() {
        this.settingsForm = this.fb.group({
            maxResults: ['', [Validators.required, minNumberValidator(0), maxNumberValidator(20)]],
            maxTransfers: ['', [Validators.required, minNumberValidator(0)]],
            day: ['', [Validators.required]],
            time: ['', [Validators.required, Validators.pattern('^([01]?[0-9]|2[0-3]):[0-5][0-9]$')]]
        });
        this.settingsForm.patchValue(this.settings);
    }
    initDayOfWeek() {
        this.translate.get('common.day.monday').subscribe(val => {
            this.daysOfWeek = [
                {'id': 2, 'name': this.translate.instant('common.day.monday')},
                {'id': 3, 'name': this.translate.instant('common.day.tuesday')},
                {'id': 4, 'name': this.translate.instant('common.day.wednesday')},
                {'id': 5, 'name': this.translate.instant('common.day.thursday')},
                {'id': 6, 'name': this.translate.instant('common.day.friday')},
                {'id': 7, 'name': this.translate.instant('common.day.saturday')},
                {'id': 1, 'name': this.translate.instant('common.day.sunday')}
            ];
            let curDay = new Date().getDay() + 1;
            this.settings.day = curDay;
            this.settings.time = new Date().getHours() + ':' + new Date().getMinutes();
            this.settingsForm.patchValue(this.settings);
        });

    }
    onSubmit() {
        if (!this.settingsForm.valid) {
            return;
        } else {
            // TODO
        }
    }
    getSettings(): SearchSettings {
        return this.settings;
    }
}
