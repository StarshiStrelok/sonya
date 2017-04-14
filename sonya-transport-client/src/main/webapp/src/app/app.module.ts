import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {HttpModule} from '@angular/http';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';

import {SSMaterialModule} from './lib/module/material/module';
import {SSValidatorModule} from './lib/module/validator/module';

import {AppComponent} from './app.component';
import {TransportProfileForm} from './form/transport-profile/transport-profile.form';

@NgModule({
    declarations: [
        AppComponent,
        TransportProfileForm
    ],
    imports: [
        BrowserModule,
        FormsModule,
        ReactiveFormsModule,
        HttpModule,
        BrowserAnimationsModule,
        SSMaterialModule,
        SSValidatorModule
    ],
    providers: [],
    bootstrap: [AppComponent]
})
export class AppModule {}
