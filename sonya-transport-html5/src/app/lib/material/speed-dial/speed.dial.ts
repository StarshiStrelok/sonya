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

import {
    Component, ContentChild, ContentChildren, QueryList, AfterContentInit,
    Input, ViewEncapsulation
} from '@angular/core';
import {trigger, state, style, animate, transition} from '@angular/animations';
import {MdButton} from '@angular/material'

const scaleAnim: any =
    trigger('scaleAnim', [
        state('show', style({opacity: 1, transform: `scale(1)`})),
        state('hide', style({opacity: 0, transform: `scale(0)`})),
        transition('show => hide', [
            animate(`0.2s 100ms ease-in`)
        ]),
        transition('hide => show', [
            animate(`0.2s 100ms ease-in`)
        ])
    ])

@Component({
    selector: 'md-fab-trigger',
    template: `<ng-content></ng-content>`,
    encapsulation: ViewEncapsulation.None
})
export class MdFabTrigger implements AfterContentInit {
    @ContentChild(MdButton) triggerBtn: MdButton;
    private parent: MdFabSpeedDial;
    setParent(p: MdFabSpeedDial) {
        this.parent = p;
    }
    ngAfterContentInit() {
        let _comp = this;
        this.triggerBtn._getHostElement().onclick = function () {
            _comp.parent.actions.toggle();
        }
    }
}

@Component({
    selector: 'md-fab-action',
    template: `<button md-mini-fab color="warn" [@scaleAnim]="state"
                    (@scaleAnim.start)="startAnimation($event)">
                <ng-content></ng-content>
            </button>`,
    encapsulation: ViewEncapsulation.None,
    animations: [scaleAnim]
})
export class MdFabActionButton implements AfterContentInit {
    private state: string = 'hide';
    private items: QueryList<MdFabActionButton>;
    private curIndex: number;
    private isOpen: boolean;
    private delay: number;
    ngAfterContentInit() {

    }
    fireToggle(isOpen: boolean, items: QueryList<MdFabActionButton>, index: number, delay: number) {
        items.toArray()[index].state = isOpen ? 'show' : 'hide';
        this.items = items;
        this.curIndex = index;
        this.isOpen = isOpen;
        this.delay = delay;
    }
    startAnimation() {
        let _comp = this;
        setTimeout(function() {
            _comp.triggerNextAnimation(_comp);
        }, _comp.delay / 2.5);
    }
    private triggerNextAnimation(comp: MdFabActionButton) {
        if (comp.items) {
            let nextIdx = comp.isOpen ? comp.curIndex + 1 : comp.curIndex - 1;
            if (comp.items.length === nextIdx || nextIdx < 0) {
                return;
            }
            comp.items.toArray()[nextIdx].fireToggle(comp.isOpen, comp.items, nextIdx, comp.delay / 2);
        }
    }
}

@Component({
    selector: 'md-fab-actions',
    template: `<ng-content></ng-content>`,
    encapsulation: ViewEncapsulation.None
})
export class MdFabActions implements AfterContentInit {
    @ContentChildren(MdFabActionButton) items: QueryList<MdFabActionButton>;
    private isOpen: boolean = false;
    ngAfterContentInit() {

    }
    toggle() {
        this.isOpen = !this.isOpen;
        if (this.isOpen) {
            this.items.first.fireToggle(this.isOpen, this.items, 0, 200);
        } else {
            this.items.last.fireToggle(this.isOpen, this.items, this.items.length - 1, 200);
        }
    }
}

@Component({
    selector: 'md-fab-speed-dial',
    template: ` <div class="speed-dial-content" [ngClass]="directionClass">
                    <ng-content></ng-content>
                </div>`,
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
        this.trigger.setParent(this);
    }
}

