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

export interface AbsModel {
}
export class ModelClass {
    static TRANSPORT_PROFILE = 'transport-profile';
    static BUS_STOP = 'busstop';
    static ROUTE = 'route';
    static PATH = 'path';
}

export class BusStop implements AbsModel {
    constructor(
        public id: number,
        public name: string,
        public latitude: number,
        public longitude: number,
        public externalId: number
    ) {}
}

export class RouteProfile implements AbsModel {
    constructor(
        public id: number,
        public name: string,
        public avgSpeed: number,
        public routingURL: string,
        public lineColor: string,
        public lastUpdate: Date
    ) {}
}

export class TransportProfile implements AbsModel {
    constructor(
        public id: number,
        public southWestLat: number,
        public southWestLon: number,
        public northEastLat: number,
        public northEastLon: number,
        public initialZoom: number,
        public minZoom: number,
        public centerLat: number,
        public centerLon: number,
        public name: string,
        public busStopAccessZoneRadius: number,
        public searchLimitForPoints: number,
        public routeProfiles: RouteProfile[],
        public mapLayers: MapLayer[],
        public mapboxKey: string,
        public hasSchedule: boolean
    ) {}
}

export class MapLayer implements AbsModel {
    constructor(
        public id: number,
        public name: string,
        public url: string,
        public mdIcon: string
    ) {}
}

export class Route implements AbsModel {
    constructor(
        public id: number,
        public type: RouteProfile,
        public namePrefix: string,
        public namePostfix: string,
        public externalId: number,
        public parserName: string
    ) {}
}

export class Path implements AbsModel {
    constructor(
        public id: number,
        public description: string,
        public externalId: number,
        public busstops: BusStop[],
        public transportProfile: TransportProfile,
        public route: Route
    ) {}
}
export class ImportDataEvent {
    constructor(
        public trigger: string,
        public type: string,
        public info: any
    ) {}
}

export class SearchSettings {
    constructor(
        public startLat: number,
        public startLon: number,
        public endLat: number,
        public endLon: number,
        public profileId: number,
        public day: number,
        public time: string,
        public maxResults: number,
        public maxTransfers: number
    ) {}
}

export class OptimalPath {
    constructor(
        public path: Path[],
        public way: BusStop[][],
        public time: number,
        public distance: number,
        public schedule: OptimalSchedule
    ) {}
}

export class OptimalSchedule {
    constructor(
        public startDate: number,
        public arrivalDate: number,
        public duration: number,
        public data: BusStopTime[][]
    ) {}
}

export class BusStopTime {
    constructor(
        public busstop: string,
        public time: number
    ) {}
}

export class Trip {
    constructor(
        public id: number,
        public regular: string,
        public irregular: string,
        public days: string
    ) {}
}