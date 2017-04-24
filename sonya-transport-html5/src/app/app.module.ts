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
import {TransportProfileMap, SideNavContentDirective} from './component/transport-profile.map';
import {BusStopForm} from './form/bus-stop.form';
import {RouteForm} from './form/route.form';
import {RoutesGrid} from './component/routes.grid';
import {PathsGrid} from './component/paths.grid';
import {BusStopGrid} from './component/busstop.grid';
import {ConfirmImport} from './component/confirm.import.dialog';
import {PathForm} from './form/path.form';
import {Window, WindowDirective} from './component/window';

import {DataService} from './service/data.service';
import {DialogService} from './service/dialog.service';
import {OSRMService} from './service/osrm.service'

@NgModule({
    declarations: [
        AppComponent,
        TransportProfileForm,
        TransportProfileList,
        TransportProfileMap,
        BusStopForm,
        RouteForm,
        RoutesGrid,
        PathsGrid,
        BusStopGrid,
        PathForm,
        ConfirmImport,
        Window,
        WindowDirective,
        SideNavContentDirective
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
        Window, BusStopForm, RouteForm, RoutesGrid, PathsGrid, PathForm, BusStopGrid, ConfirmImport
    ],
    providers: [DataService, DialogService, OSRMService],
    bootstrap: [AppComponent]
})
export class AppModule {}
