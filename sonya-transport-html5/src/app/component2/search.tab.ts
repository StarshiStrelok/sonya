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

import {Component, ViewChild, Input} from '@angular/core';
import {MdTabGroup} from '@angular/material';

import {SearchSettingsForm} from './search.settings.form';
import {SearchResultList} from './search.result.list';
import {TransportMap} from './transport.map';

@Component({
    selector: 'search-tab',
    templateUrl: './search.tab.html',
    styles: [`.st-panel {
        margin: 10px;
        padding: 10px;
    }
    .st-h-i {
        font-size: 30px;
        margin-top: 5px;
    }
    `]
})
export class SearchTab {
    @ViewChild(SearchSettingsForm) searchSettings: SearchSettingsForm;
    @ViewChild(SearchResultList) searchResult: SearchResultList;
    @ViewChild(MdTabGroup) tabGroup: MdTabGroup;
    @Input() mapComponent: TransportMap;
}
