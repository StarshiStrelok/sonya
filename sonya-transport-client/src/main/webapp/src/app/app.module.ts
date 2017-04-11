import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {HttpModule} from '@angular/http';

import {AppComponent} from './app.component';
import { TransportProfileComponent } from './form/transport-profile/transport-profile.component';
import { NumberComponent } from './lib/input/number/number.component';

@NgModule({
    declarations: [
        AppComponent,
        TransportProfileComponent,
        NumberComponent
    ],
    imports: [
        BrowserModule,
        FormsModule,
        HttpModule
    ],
    providers: [],
    bootstrap: [AppComponent]
})
export class AppModule {}
