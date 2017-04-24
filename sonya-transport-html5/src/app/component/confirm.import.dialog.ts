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
import {MdDialogRef} from '@angular/material';

import {ImportDataEvent} from '../model/abs.model';
import {Window, DialogContent} from '../component/window';

@Component({
    selector: 'confirm-import-dialog',
    templateUrl: './confirm.import.dialog.html',
})
export class ConfirmImport extends DialogContent implements OnInit {
    events: ImportDataEvent[];
    persist: boolean
    ngOnInit() {
        
    }
    // ========================================================================
    setDialogRef(dialogRef: MdDialogRef<Window>): void {
        this.dialogRef = dialogRef;
    }
    setData(data: any): void {
        this.events = data.events;
        this.persist = data.persist;
    }
}
