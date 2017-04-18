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

import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';

import {DataService} from '../service/data.service';
import {TransportProfile} from '../model/transport-profile';
import {ModelClass} from '../model/abs.model';
import {Links} from '../route.module';
import {DialogService} from '../service/dialog.service';

@Component({
    selector: 'transport-profile-list',
    templateUrl: './transport-profile.list.html'
})
export class TransportProfileList implements OnInit {
    private profiles: TransportProfile[];
    constructor(
        private dataService: DataService,
        private dialogService: DialogService,
        private router: Router
    ) {};
    ngOnInit() {
        this.loadProfiles();
    }
    loadProfiles() {
        this.dataService.getAll<TransportProfile>(ModelClass.TRANSPORT_PROFILE)
            .then((profiles: TransportProfile[]) => this.profiles = profiles);
    }
    newProfile() {
        this.router.navigate([Links.PROFILE_FORM]);
    }
    editProfile(id: number) {
        this.router.navigate([Links.PROFILE_FORM, id]);
    }
    deleteProfile(id: number) {
        console.log('delete profile [' + id + ']');
        this.dialogService.confirm(
            'Delete profile',
            'Are you sure that you want delete this profile?'
        ).subscribe((result) => {
            if (result) {
                this.dataService.deleteById(id, ModelClass.TRANSPORT_PROFILE)
                .then((result: boolean) => {
                    if (result) {
                        this.loadProfiles();
                    }
                });
            }
        });
    }
    openMap(id: number) {
        console.log('open profile map [' + id + ']');
        this.router.navigate([Links.PROFILE_MAP, id]);
    }
}

