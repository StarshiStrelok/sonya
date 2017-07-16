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
import {MdDialogRef} from '@angular/material';
import {DataService} from '../service/data.service';

import {Window, DialogContent} from '../component/window';

@Component({
    selector: 'stat-filter-panel',
    template: `<div [innerHtml]="info"></div>`
})
export class StatFilterPanel extends DialogContent implements OnInit {
    info = '';
    constructor(
        private dataService: DataService
    ) {super()}
    ngOnInit() {
        this.dataService.statFilter().then(res => this.info = res);
    }
    setData(data: any): void {
    }
    setDialogRef(dialogRef: MdDialogRef<Window>): void {
        this.dialogRef = dialogRef;
    }
}
