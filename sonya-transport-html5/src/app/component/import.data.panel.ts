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
import {ActivatedRoute, Params} from '@angular/router';

import {DataService} from '../service/data.service';
import {DialogService} from '../service/dialog.service';
import {slideAnimation, AnimatedSlide} from './../app.component';

@Component({
    selector: 'import-data-panel',
    templateUrl: './import.data.panel.html',
    animations: [slideAnimation]
})
export class ImportDataPanel extends AnimatedSlide implements OnInit {
    profileId: number;
    constructor(
        private dataService: DataService,
        private dialogService: DialogService,
        private activatedRoute: ActivatedRoute
    ) {super()};
    ngOnInit() {
        this.activatedRoute.params.subscribe((params: Params) => {
            let id = params['id'];
            this.profileId = id;
        });
    }
}
