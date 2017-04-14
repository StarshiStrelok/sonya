import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {HttpModule} from '@angular/http';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';

import {AngularMaterialModule} from './angular-material.module'

import {AppComponent} from './app.component';
import {MaxNumberDirective} from './directive/validator/max-number.directive';
import {MinNumberDirective} from './directive/validator/min-number.directive';
import {TransportProfileForm} from './form/transport-profile/transport-profile.form';
import {LatitudeField} from './lib/input/latitude-field/latitude-field';
import {LongitudeField} from './lib/input/longitude-field/longitude-field';

@NgModule({
    declarations: [
        AppComponent,
        MaxNumberDirective,
        MinNumberDirective,
        TransportProfileForm,
        LatitudeField,
        LongitudeField
    ],
    imports: [
        BrowserModule,
        FormsModule,
        ReactiveFormsModule,
        HttpModule,
        BrowserAnimationsModule,
        AngularMaterialModule
    ],
    providers: [],
    bootstrap: [AppComponent]
})
export class AppModule {}
