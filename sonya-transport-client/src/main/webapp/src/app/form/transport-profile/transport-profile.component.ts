import {Component, OnInit} from '@angular/core';
import {TransportProfile} from '../../model/transport-profile'

@Component({
    selector: 'transport-profile-form',
    templateUrl: './transport-profile.component.html',
    styleUrls: ['./transport-profile.component.css']
})
export class TransportProfileComponent implements OnInit {
    constructor() {}
    ngOnInit() {
    }
    model = new TransportProfile(null, null, null, null, null, null, null, null, null, null);
    submitted = false;
    onSubmit() {
        this.submitted = true;
    }
    get diagnostic() {
        return JSON.stringify(this.model);
    }
}
