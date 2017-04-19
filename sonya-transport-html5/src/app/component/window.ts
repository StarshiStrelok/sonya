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
    Component, Directive, ViewContainerRef, ViewChild,
    ComponentFactoryResolver, AfterViewInit, Type
} from '@angular/core';
import {MdDialogRef} from '@angular/material';

@Directive({
    selector: '[win-content]',
})
export class WindowDirective {
    constructor(public viewContainerRef: ViewContainerRef) {}
}

@Component({
    selector: 'window-dialog',
    templateUrl: './window.html',
    styles: [`.window-title {
                text-align: center;
                font-weight: 500;
                font-family: Roboto,"Helvetica Neue",sans-serif;
            }`]
})
export class Window implements AfterViewInit {
    @ViewChild(WindowDirective) directive: WindowDirective;
    public title: string;
    public compType: Type<DialogContent>;
    public data: any;

    constructor(
        public dialogRef: MdDialogRef<Window>,
        private resolver: ComponentFactoryResolver
    ) {}

    ngAfterViewInit() {
        let _comp = this;
        setTimeout(function() {
            let componentFactory = _comp.resolver.resolveComponentFactory(_comp.compType);
            let viewContainerRef = _comp.directive.viewContainerRef;
            viewContainerRef.clear();
            let componentRef = viewContainerRef.createComponent(componentFactory);
            (componentRef.instance).setDialogRef(_comp.dialogRef);
            (componentRef.instance).setData(_comp.data);
        });
    }
}

export abstract class DialogContent {
    dialogRef: MdDialogRef<Window>;
    setDialogRef(dialogRef: MdDialogRef<Window>): void {
        this.dialogRef = dialogRef;
    }
    abstract setData(data: any): void;
}

