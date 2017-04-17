import {AbsModel} from './abs.model';
import {RouteProfile} from './route-profile';

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
        public routeProfiles: RouteProfile[]
    ) {}
}
