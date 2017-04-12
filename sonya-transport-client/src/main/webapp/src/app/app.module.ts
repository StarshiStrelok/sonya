import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {HttpModule} from '@angular/http';

import {AppComponent} from './app.component';
import {TransportProfileComponent} from './form/transport-profile/transport-profile.component';
import {NumberComponent} from './lib/input/number/number.component';
import {MaxNumberDirective} from './directive/validator/max-number.directive';
import {MinNumberDirective} from './directive/validator/min-number.directive';
import { TextComponent } from './lib/input/text/text.component';

@NgModule({
    declarations: [
        AppComponent,
        TransportProfileComponent,
        NumberComponent,
        MaxNumberDirective,
        MinNumberDirective,
        TextComponent
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
