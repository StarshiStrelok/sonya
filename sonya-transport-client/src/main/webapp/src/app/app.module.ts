import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {HttpModule} from '@angular/http';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';

import {AppRoutingModule} from './route.module';
import {SSMaterialModule} from './lib/module/material/module';
import {SSValidatorModule} from './lib/module/validator/module';

import {AppComponent} from './app.component';
import {TransportProfileForm} from './form/transport-profile/transport-profile.form';
import {TransportProfileList} from './component/transport-profile-list/transport-profile.list';
import {TransportProfileMap} from './component/transport-profile-map/transport-profile.map';

import {DataService} from './service/data.service';
import {DialogService} from './service/dialog.service';

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
    providers: [DataService, DialogService],
    bootstrap: [AppComponent]
})
export class AppModule {}
