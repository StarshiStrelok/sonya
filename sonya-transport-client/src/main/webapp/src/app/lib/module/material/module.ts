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

import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {CommonModule} from '@angular/common';
import {BrowserModule} from '@angular/platform-browser';
import {
    MdButtonModule,
    MdCheckboxModule,
    MdInputModule,
    MdToolbarModule,
    MdCardModule,
    MdIconModule,
    MdGridListModule,
    MdListModule,
    MdMenuModule,
    MdTooltipModule,
    MdDialogModule
} from '@angular/material';
import {LatitudeField} from './component/latitude-field/latitude-field';
import {LongitudeField} from './component/longitude-field/longitude-field';
import {ZoomField} from './component/zoom-field/zoom-field';
import {ConfirmDialog} from './component/dialog/confirm.dialog';

@NgModule({
    imports: [
        MdButtonModule,
        MdCheckboxModule,
        MdInputModule,
        MdToolbarModule,
        MdCardModule,
        MdIconModule,
        MdGridListModule,
        MdListModule,
        MdMenuModule,
        MdTooltipModule,
        MdDialogModule,
        FormsModule,
        ReactiveFormsModule,
        BrowserModule,
        CommonModule
    ],
    exports: [
        MdButtonModule,
        MdCheckboxModule,
        MdInputModule,
        MdToolbarModule,
        MdCardModule,
        MdIconModule,
        MdGridListModule,
        MdListModule,
        MdMenuModule,
        MdTooltipModule,
        MdDialogModule,
        LatitudeField, LongitudeField, ZoomField, ConfirmDialog
    ],
    declarations: [
        LatitudeField, LongitudeField, ZoomField, ConfirmDialog
    ],
    entryComponents: [
        ConfirmDialog
    ]
})
export class SSMaterialModule {}
