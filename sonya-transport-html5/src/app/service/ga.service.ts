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


import {Injectable} from '@angular/core';
declare var ga: any;

export class EventCategory {
    public static TRANSPORT = 'transport';
    public static INTERFACE = 'interface';
}

export class EventAction {
    // transport
    public static GEOCODER_SEARCH = 'geocoder_search';
    public static ROUTES_SEARCH = 'routes_search';
    public static SHOW_SCHEDULE_TABLE = 'show_schedule_table';
    public static SWITCH_PROFILE = 'switch_profile';
    // interface
    public static SWITCH_THEME = 'switch_theme';
    public static SWITCH_LANG = 'switch_language';
    public static SWITCH_MAP_LAYER = 'switch_map_layer';
}

@Injectable()
export class GAService {
    public sendEvent(category: EventCategory, action: EventAction) {
        ga('send', 'event', {
            eventCategory: category,
            eventAction: action,
            eventLabel: ''
        });
    }
}