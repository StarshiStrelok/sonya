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
    Input, ViewEncapsulation, EventEmitter, Output
} from '@angular/core';
import {trigger, state, style, animate, transition} from '@angular/animations';

const scaleAnim: any =
    trigger('scaleAnim', [
        state('show', style({opacity: 1, transform: `scale(1)`, display: `inherit`})),
        state('hide', style({opacity: 0, transform: `scale(0)`, display: `none`})),
        transition('show => hide', [
            animate(`0.1s ease-in`)
        ]),
        transition('hide => show', [
            animate(`0.2s ease-in`)
        ])
    ])

const rotateAnim: any =
    trigger('rotateAnim', [
        state('open', style({transform: `rotate(360deg)`})),
        state('close', style({transform: `rotate(-360deg)`})),
        transition('open => close', [
            animate(`1s cubic-bezier(.4,0,.2,1)`)
        ]),
        transition('close => open', [
            animate(`1s cubic-bezier(.4,0,.2,1)`)
        ])
    ])

@Component({
    selector: 'md-fab-trigger',
    template: ` <button md-fab color="primary" (click)="toggleSpeedDial()"
                    [@rotateAnim]="animationState">
                    <ng-content></ng-content>
                </button>`,
    encapsulation: ViewEncapsulation.None,
    animations: [rotateAnim]
})
export class MdFabTrigger implements AfterContentInit {
    private parent: MdFabSpeedDial;
    animationState: string = 'close';
    setParent(p: MdFabSpeedDial) {
        this.parent = p;
    }
    ngAfterContentInit() {

    }
    toggleSpeedDial() {
        this.parent.actions.toggle();
        this.rotateTrigger();
    }
    rotateTrigger() {
        this.animationState = this.animationState === 'close' ? 'open' : 'close';
    }
}

@Component({
    selector: 'md-fab-action',
    template: `<button md-mini-fab [@scaleAnim]="state"
                    (@scaleAnim.start)="startAnimation($event)" (click)="select()">
                <ng-content></ng-content>
            </button>`,
    encapsulation: ViewEncapsulation.None,
    animations: [scaleAnim]
})
export class MdFabActionButton implements AfterContentInit {
    private parent: MdFabActions;
    state: string = 'hide';
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
    select() {
        this.parent.toggle();
        this.parent.parent.selectAction.emit(this.curIndex);
        this.parent.parent.trigger.rotateTrigger();
    }
    setParent(p: MdFabActions) {
        this.parent = p;
    }
    startAnimation(event: any) {
        let _comp = this;
        setTimeout(function () {
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
    parent: MdFabSpeedDial;
    private isOpen: boolean = false;
    ngAfterContentInit() {
        this.initChilds();
        this.items.changes.subscribe(
            o => this.initChilds()
        );
    }
    private initChilds() {
        this.items.forEach(item => {
            item.setParent(this);
        });
    }
    toggle() {
        this.isOpen = !this.isOpen;
        if (this.isOpen) {
            this.items.first.fireToggle(this.isOpen, this.items, 0, 200);
        } else {
            this.items.last.fireToggle(this.isOpen, this.items, this.items.length - 1, 100);
        }
    }
    setParent(p: MdFabSpeedDial) {
        this.parent = p;
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
    @Output() selectAction: EventEmitter<any> = new EventEmitter();
    directionClass = 'md-up';
    @Input()
    get direction() {
        return this.directionClass;
    }
    set direction(d: string) {
        this.directionClass = 'md-' + d;
    }
    ngAfterContentInit() {
        this.trigger.setParent(this);
        this.actions.setParent(this);
    }
}

