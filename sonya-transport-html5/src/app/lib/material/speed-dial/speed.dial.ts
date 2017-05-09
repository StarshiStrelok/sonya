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

import {Component, ContentChildren, ViewChild, ContentChild,
    AfterContentInit, Input, ViewEncapsulation} from '@angular/core';
import {MdButton} from '@angular/material'

@Component({
    selector: 'md-fab-trigger',
    templateUrl: './speed.dial.trigger.html'
})
export class MdFabTrigger implements AfterContentInit {
    @ContentChild(MdButton) triggerBtn: MdButton;
    ngAfterContentInit() {
        
    }
}

@Component({
    selector: 'md-fab-actions',
    templateUrl: './speed.dial.action.html'
})
export class MdFabActions {
}

@Component({
    selector: 'md-fab-speed-dial',
    templateUrl: './speed.dial.html',
    styleUrls: ['./speed.dial.css'],
    encapsulation: ViewEncapsulation.None
})
export class MdFabSpeedDial implements AfterContentInit {
    @ContentChild(MdFabActions) actions: MdFabActions;
    @ContentChild(MdFabTrigger) trigger: MdFabTrigger;
    private directionClass = 'md-up';
    
    @Input()
    get direction() {
        return this.directionClass;
    }
    set direction(d: string) {
        this.directionClass = 'md-' + d;
    }
    
    ngAfterContentInit() {
        
    }
}

