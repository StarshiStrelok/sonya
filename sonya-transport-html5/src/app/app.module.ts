import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {HttpModule} from '@angular/http';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';

import {AppRoutingModule} from './route.module';
import {SSMaterialModule} from './lib/material/module';
import {SSValidatorModule} from './lib/validator/module';

import {AppComponent} from './app.component';
import {TransportProfileForm} from './form/transport-profile.form';
import {TransportProfileList} from './component/transport-profile.list';
import {TransportProfileMap} from './component/transport-profile.map';

import {DataService} from './service/data.service';
import {DialogService} from './service/dialog.service';
import {LeafletService} from './service/leaflet.service';

@NgModule({
    declarations: [
        AppComponent,
        TransportProfileForm,
        TransportProfileList,
        TransportProfileMap
    ],
    imports: [
        BrowserModule,
        FormsModule,
        ReactiveFormsModule,
        HttpModule,
        BrowserAnimationsModule,
        SSMaterialModule,
        SSValidatorModule,
        AppRoutingModule
    ],
    providers: [DataService, DialogService, LeafletService],
    bootstrap: [AppComponent]
})
export class AppModule {}
