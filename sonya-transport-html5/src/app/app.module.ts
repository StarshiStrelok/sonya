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
import {BusStopForm} from './form/bus-stop.form';
import {Window, WindowDirective} from './component/window';

import {DataService} from './service/data.service';
import {DialogService} from './service/dialog.service';

@NgModule({
    declarations: [
        AppComponent,
        TransportProfileForm,
        TransportProfileList,
        TransportProfileMap,
        BusStopForm,
        Window,
        WindowDirective
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
    entryComponents: [
        Window, BusStopForm
    ],
    providers: [DataService, DialogService],
    bootstrap: [AppComponent]
})
export class AppModule {}
