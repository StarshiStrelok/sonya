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

import {Observable} from 'rxjs/Rx';
import {MdDialogRef, MdDialog} from '@angular/material';
import {Injectable, Type} from '@angular/core';

import {ConfirmDialog} from './../lib/material/confirm.dialog';
import {Window} from './../component/window';

@Injectable()
export class DialogService {

    constructor(private dialog: MdDialog) {}

    public confirm(title: string, message: string): Observable<boolean> {
        let dialogRef: MdDialogRef<ConfirmDialog>;

        dialogRef = this.dialog.open(ConfirmDialog);
        dialogRef.componentInstance.title = title;
        dialogRef.componentInstance.message = message;

        return dialogRef.afterClosed();
    }
    public openWindow(title: string, height: string, width: string,
            cType: Type<any>, data: any): Observable<boolean> {
        let dialogRef: MdDialogRef<Window>;

        dialogRef = this.dialog.open(Window, {
            height: height,
            width: width,
        });
        dialogRef.componentInstance.title = title;
        dialogRef.componentInstance.compType = cType;
        dialogRef.componentInstance.data = data;

        return dialogRef.afterClosed();
    }
}
