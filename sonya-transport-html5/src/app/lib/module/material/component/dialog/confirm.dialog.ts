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

import {Component} from '@angular/core';
import {MdDialogRef} from '@angular/material';

@Component({
    selector: 'confirm-dialog',
    templateUrl: './confirm.dialog.html',
    styles: [
        '.cd-hr { margin-bottom: 0px; margin-top: 0px;}'
    ]
})
export class ConfirmDialog {
    public title: string;
    public message: string;

    constructor(public dialogRef: MdDialogRef<ConfirmDialog>) {

    }
}
