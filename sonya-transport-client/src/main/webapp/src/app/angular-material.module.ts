import {NgModule} from '@angular/core';
import {
    MdButtonModule,
    MdCheckboxModule,
    MdInputModule,
    MdToolbarModule,
    MdCardModule,
    MdIconModule,
    MdGridListModule
} from '@angular/material';

@NgModule({
    imports: [
        MdButtonModule,
        MdCheckboxModule,
        MdInputModule,
        MdToolbarModule,
        MdCardModule,
        MdIconModule,
        MdGridListModule
    ],
    exports: [
        MdButtonModule,
        MdCheckboxModule,
        MdInputModule,
        MdToolbarModule,
        MdCardModule,
        MdIconModule,
        MdGridListModule
    ]
})
export class AngularMaterialModule {}
