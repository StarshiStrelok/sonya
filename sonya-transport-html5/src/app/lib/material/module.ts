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
    MdDialogModule,
    MdSelectModule,
    MdSidenavModule,
    MdChipsModule,
    MdProgressSpinnerModule,
    MdProgressBarModule,
    MdAutocompleteModule,
    MdSnackBarModule,
    MdTabsModule,
    MdSlideToggleModule
} from '@angular/material';

import {LatitudeField} from './latitude-field';
import {LongitudeField} from './longitude-field';
import {ZoomField} from './zoom-field';
import {ConfirmDialog} from './confirm.dialog';
import {Waiting} from './waiting';
import {MdFabSpeedDial, MdFabTrigger, MdFabActions, MdFabActionButton} from './speed-dial/speed.dial';

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
        MdSelectModule,
        MdSidenavModule,
        MdChipsModule,
        MdProgressSpinnerModule,
        MdProgressBarModule,
        MdAutocompleteModule,
        MdSnackBarModule,
        MdTabsModule,
        MdSlideToggleModule,
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
        MdSelectModule,
        MdSidenavModule,
        MdChipsModule,
        MdProgressSpinnerModule,
        MdProgressBarModule,
        MdAutocompleteModule,
        MdSnackBarModule,
        MdTabsModule,
        MdSlideToggleModule,
        LatitudeField, LongitudeField, ZoomField, ConfirmDialog, Waiting,
        MdFabSpeedDial, MdFabTrigger, MdFabActions, MdFabActionButton
    ],
    declarations: [
        LatitudeField, LongitudeField, ZoomField, ConfirmDialog, Waiting,
        MdFabSpeedDial, MdFabTrigger, MdFabActions, MdFabActionButton
    ],
    entryComponents: [
        ConfirmDialog
    ]
})
export class SSMaterialModule {}
