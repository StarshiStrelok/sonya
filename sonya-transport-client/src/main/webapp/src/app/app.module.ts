import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {HttpModule} from '@angular/http';

import {AppComponent} from './app.component';
import {TransportProfileComponent} from './form/transport-profile/transport-profile.component';
import {NumberComponent} from './lib/input/number/number.component';
import {MaxNumberDirective} from './directive/validator/max-number.directive';
import {MinNumberDirective} from './directive/validator/min-number.directive';
import {TransportProfileFormComponent} from './form/transport-profile-form/transport-profile-form.component';
import {TextfieldComponent} from './lib/input/textfield/textfield.component';
import { NumberfieldComponent } from './lib/input/numberfield/numberfield.component';

@NgModule({
    declarations: [
        AppComponent,
        TransportProfileComponent,
        NumberComponent,
        MaxNumberDirective,
        MinNumberDirective,
        TransportProfileFormComponent,
        TextfieldComponent,
        NumberfieldComponent
    ],
    imports: [
        BrowserModule,
        FormsModule,
        ReactiveFormsModule,
        HttpModule
    ],
    providers: [],
    bootstrap: [AppComponent]
})
export class AppModule {}
