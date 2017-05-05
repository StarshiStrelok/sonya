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

import {Component} from '@angular/core';

import {OptimalPath} from '../model/abs.model';

@Component({
    selector: 'search-result',
    templateUrl: './search.result.list.html',
    styles: [`
            .srl-route-label {
                padding: 5px;
                border-radius: 2px;
                font-weight: bold;
                margin-left: 5px;
            }
            .srl-route-label:hover {
                cursor: pointer;
            }
            `]
})
export class SearchResultList {
    result: OptimalPath[] = [];
    setResult(res: OptimalPath[]) {
        this.result = res;
    }
}
