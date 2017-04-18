/* 
 * Copyright (C) 2017 ss
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import {Component, OnInit, ViewChild, ElementRef} from '@angular/core';
import {Router, ActivatedRoute, Params} from '@angular/router';
import {Location} from '@angular/common';

import {DataService} from '../../service/data.service';
import {LeafletService} from '../../service/leaflet.service';

import {TransportProfile} from '../../model/transport-profile';
import {ModelClass} from '../../model/abs.model';

declare var L: any;

@Component({
    selector: 'transport-profile-map',
    templateUrl: './transport-profile.map.html',
    styleUrls: ['./transport-profile.map.css']
})
export class TransportProfileMap implements OnInit {
    @ViewChild('map') mapElement: ElementRef;
    private map: any;
    constructor(
        private location: Location,
        private dataService: DataService,
        private leafletService: LeafletService,
        private activatedRoute: ActivatedRoute
    ) {}
    ngOnInit() {
        this.activatedRoute.params.subscribe((params: Params) => {
            let id = params['id'];
            if (id) {
                this.dataService.findById<TransportProfile>(
                    id, ModelClass.TRANSPORT_PROFILE).then((profile: TransportProfile) => {
                        this.map = this.leafletService.createMap(profile, this.mapElement);
                        this.leafletService.addControl(this.map, 'close', this.fnBack, this, 'Close map');
                    });
            }
        });
    }
    fnBack = function (component: TransportProfileMap) {
        component.location.back();
    }
}
