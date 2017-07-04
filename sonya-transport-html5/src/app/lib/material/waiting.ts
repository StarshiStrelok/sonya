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

@Component({
    selector: 'waiting',
    template: `<div class="modal" 
                    [ngClass]="{'modal-close': !isOpen, 'modal-open': isOpen, 'modal': true}"
                    (click)="eatEvent($event)">
                    <md-spinner class="w-spin" [style.margin-top.px]="offset"></md-spinner>
               </div>`,
    styles: [`.modal {
                position: fixed;
                top: 0;
                right: 0;
                bottom: 0;
                left: 0;
                z-index: 3000;
                overflow: hidden;
                -webkit-overflow-scrolling: touch;
                outline: 0;
                background-color: rgba(255, 255, 255, 0.45);
            }
            .modal-open {
                display: block;
            }
            .modal-close {
                display: none;
            }
            .w-spin {
                margin: auto;
            }
    `]
})
export class Waiting {
    isOpen = false;
    offset = 100;
    open() {
        this.offset = window.innerHeight / 2 - 50 - 30;
        this.isOpen = true;
    }
    close() {
        this.isOpen = false;
    }
    eatEvent(event: any) {
        event.stopPropagation();
    }
}
