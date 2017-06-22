import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {HttpModule, Http} from '@angular/http';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {SimpleNotificationsModule} from 'angular2-notifications';
import {TranslateModule, TranslateLoader} from '@ngx-translate/core';
import {TranslateHttpLoader} from '@ngx-translate/http-loader';

import {AppRoutingModule} from './route.module';
import {SSMaterialModule} from './lib/material/module';
import {SSValidatorModule} from './lib/validator/module';

import {AppComponent} from './app.component';
// ---------------------- Admin components ------------------------------------
import {TransportProfileForm} from './component/form/transport-profile.form';
import {TransportProfileList} from './component/transport-profile.list';
import {TransportProfileMap, SideNavContentDirective} from './component/transport-profile.map';
import {BusStopForm} from './component/form/bus-stop.form';
import {RouteForm} from './component/form/route.form';
import {RoutesGrid} from './component/routes.grid';
import {PathsGrid} from './component/paths.grid';
import {BusStopGrid} from './component/busstop.grid';
import {ConfirmImport} from './component/confirm.import.dialog';
import {PathForm} from './component/form/path.form';
import {Window, WindowDirective} from './component/window';
import {RegistrationForm} from './security/registration.form';
// ---------------------- UI components ---------------------------------------
import {TransportMap} from './component2/transport.map';
import {GeoCoder} from './component2/geocoder';
import {SearchTab} from './component2/search.tab';
import {SearchSettingsForm} from './component2/search.settings.form';
import {SearchResultList} from './component2/search.result.list';
// ---------------------- Services --------------------------------------------
import {DataService} from './service/data.service';
import {DialogService} from './service/dialog.service';
import {OSRMService} from './service/osrm.service'
import {CookieService} from './service/cookie.service';
import {SecurityService} from './service/security.service';

// AoT requires an exported function for factories
export function HttpLoaderFactory(http: Http) {
    return new TranslateHttpLoader(http);
}

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
        RegistrationForm,
        Window,
        WindowDirective,
        SideNavContentDirective,
        
        TransportMap,
        GeoCoder,
        SearchTab,
        SearchSettingsForm,
        SearchResultList
    ],
    imports: [
        BrowserModule,
        FormsModule,
        ReactiveFormsModule,
        HttpModule,
        BrowserAnimationsModule,
        SimpleNotificationsModule.forRoot(),
        TranslateModule.forRoot({
            loader: {
                provide: TranslateLoader,
                useFactory: HttpLoaderFactory,
                deps: [Http]
            }
        }),
        SSMaterialModule,
        SSValidatorModule,
        AppRoutingModule
    ],
    entryComponents: [
        Window, BusStopForm, RouteForm, RoutesGrid, PathsGrid, PathForm, BusStopGrid, ConfirmImport,
        RegistrationForm
    ],
    providers: [DataService, DialogService, OSRMService, CookieService, SecurityService],
    bootstrap: [AppComponent]
})
export class AppModule {}
