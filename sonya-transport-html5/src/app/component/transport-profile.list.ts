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
import {NotificationsService} from 'angular2-notifications';

import {DataService} from '../service/data.service';
import {TransportProfile} from '../model/abs.model';
import {ModelClass} from '../model/abs.model';
import {Links} from '../links';
import {DialogService} from '../service/dialog.service';
import {slideAnimation, AnimatedSlide} from './../app.component';

@Component({
    selector: 'transport-profile-list',
    templateUrl: './transport-profile.list.html',
    styles: [`.list-item {
                border: 1px groove;
                margin-bottom: 3px;
            }`],
    animations: [slideAnimation]
})
export class TransportProfileList extends AnimatedSlide implements OnInit {
    profiles: TransportProfile[];
    constructor(
        private dataService: DataService,
        private dialogService: DialogService,
        private router: Router,
        private notificationService: NotificationsService
    ) {super()};
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
        this.router.navigate([Links.PROFILE_MAP, id]);
    }
    globalUpdate() {
        this.dataService.globalUpdate().then(res => {
            this.notificationService.info('Success',
                'See server log for details');
        });
    }
}

