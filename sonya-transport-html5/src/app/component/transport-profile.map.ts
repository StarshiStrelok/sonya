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

import {
    Component, OnInit, ViewChild, ElementRef, Directive,
    ViewContainerRef, ComponentFactoryResolver, Type
} from '@angular/core';
import {ActivatedRoute, Params} from '@angular/router';
import {Location} from '@angular/common';
import {MdSidenav} from '@angular/material';

import {DataService} from '../service/data.service';
import {DialogService} from '../service/dialog.service';

import {TransportProfile, BusStop, ModelClass, RouteProfile} from '../model/abs.model';
import {BusStopForm} from './../form/bus-stop.form';
import {RoutesGrid} from './routes.grid';
import {BusStopGrid} from './busstop.grid';
import {OSRMService} from '../service/osrm.service';
import {slideAnimation, AnimatedSlide} from './../app.component';

declare var L: any;

@Directive({
    selector: '[sidenav-content]',
})
export class SideNavContentDirective {
    constructor(public viewContainerRef: ViewContainerRef) {}
}

export interface SwitchedContent {
    setData(data: any): void;
}

export class CtxMenuItem {
    constructor(
        public icon: string,
        public label: string,
        public onclick: Function,
        public component: any
    ) {}
}

@Component({
    selector: 'transport-profile-map',
    templateUrl: './transport-profile.map.html',
    styleUrls: ['./transport-profile.map.css'],
    animations: [slideAnimation]
})
export class TransportProfileMap extends AnimatedSlide implements OnInit {
    // references
    @ViewChild('map') mapElement: ElementRef;
    @ViewChild('sidenav') sideNav: MdSidenav;
    @ViewChild(SideNavContentDirective) sideNavTmpl: SideNavContentDirective;
    viewInstance: SwitchedContent;
    showProgress = false;
    // constants
    MOCK_BS: string = 'mock';
    EARTH_RAD: number = 6371; // km
    // laflet variables
    map: any;
    ctxMenu: any;           // map context menu
    ctxMenuMarker: any;     // marker context menu
    coords: any;
    layerBusStop: any = L.layerGroup([]);
    layerRouting: any = L.layerGroup([]);
    // transport profile ID
    profileId: number;
    allBusStops: BusStop[] = [];

    constructor(
        private location: Location,
        private dataService: DataService,
        private dialogService: DialogService,
        private activatedRoute: ActivatedRoute,
        private resolver: ComponentFactoryResolver,
        private osrmService: OSRMService
    ) {super()}
    ngOnInit() {
        this.activatedRoute.params.subscribe((params: Params) => {
            let id = params['id'];
            this.profileId = id;
            this.dataService.findById<TransportProfile>(
                id, ModelClass.TRANSPORT_PROFILE).then((profile: TransportProfile) => {
                    this.createMap(profile, this.mapElement);

                    this.ctxMenu = this.createContextMenu(180, [
                        new CtxMenuItem('add_circle_outline', 'Add bus stop', this.fnOpenCreateBusStopDialog, this)
                    ]);
                    this.ctxMenuMarker = this.createContextMenu(180, [
                        new CtxMenuItem('mode_edit', 'Edit bus stop', this.fnOpenEditBusStopDialog, this),
                        new CtxMenuItem('delete_forever', 'Delete bus stop', this.fnDeleteBusStop, this)
                    ]);
                    this.loadBusStops();
                });
        });
    }
    loadBusStops(): void {
        this.dataService.getFromProfile<BusStop>(this.profileId, ModelClass.BUS_STOP)
            .then((all: BusStop[]) => {
                console.log('profile bus stops loaded [' + all.length + ']');
                this.allBusStops = all;
                this.mapInteract();
            });
    }
    moveBusStop(model: BusStop, lat: number, lon: number) {
        this.dialogService.confirm('Move bus stop', 'Are you sure that you want replace this bus stop?')
            .subscribe((res: boolean) => {
                if (res) {
                    model.latitude = lat;
                    model.longitude = lon;
                    model['transportProfile'] = {id: this.profileId}
                    if (this.isBusStopGrid()) {
                        let bsGrid: BusStopGrid = <BusStopGrid> this.viewInstance;
                        let clone: BusStop[] = bsGrid.busstops.filter(bs => bs.id === model.id);
                        if (clone.length > 0) {
                            clone[0].latitude = lat;
                            clone[0].longitude = lon;
                            this.drawRoute(bsGrid.busstops, bsGrid.path.route.type);
                        }
                    }
                    this.dataService.update<BusStop>(model, ModelClass.BUS_STOP)
                        .then(() => {
                            this.loadBusStops();
                        });
                } else {
                    this.loadBusStops();
                }
            });
    }
    // =========================== controls actions ===========================
    goBack() {
        this.location.back();
    }
    openControl() {
        this.switchSideNavContent<RoutesGrid>(RoutesGrid, {
            mapComponent: this
        });
        this.sideNav.toggle();
    }
    drawRoute(way: BusStop[], routeSettings: RouteProfile) {
        this.layerRouting.clearLayers();
        if (way.length < 2) {
            return;
        }
        this.osrmService.requestPath(way, routeSettings.routingURL).then(resp => {
            let lineColor = routeSettings.lineColor;
            var legs = resp.routes[0].legs;
            let reverseCoords: number[][] = [];
            legs.forEach(leg => {
                leg.steps.forEach(step => {
                    step.geometry.coordinates.forEach(ll => {
                        reverseCoords.push([ll[1], ll[0]]);
                    });
                });
            });
            var opts = [{
                color: 'black',
                opacity: 0.15,
                weight: 9
            }, {
                color: 'white',
                opacity: 0.8,
                weight: 6
            }, {
                color: lineColor ? lineColor : 'red',
                opacity: 1,
                weight: 2,
                snaking: true
            }];
            opts.forEach(options => {
                this.layerRouting.addLayer(L.polyline(reverseCoords, options));
            });
            way.forEach(bs => {
                opts.forEach(options => {
                    this.layerRouting.addLayer(
                        L.circleMarker(new L.LatLng(bs.latitude, bs.longitude), options)
                    );
                });
            });
            this.fitBoundsByMarkers(way, -400);     // sidenav width
        });
    }
    // ========================================================================
    fnOpenCreateBusStopDialog = function (component: TransportProfileMap) {
        component.dialogService.openWindow('New bus stop', '', '50%', BusStopForm, {
            profileId: component.profileId,
            model: new BusStop(null, null, component.coords.lat, component.coords.lng, null)
        }).subscribe((res: boolean) => {
            if (res) {
                component.loadBusStops();
            }
        });
    }
    fnOpenEditBusStopDialog = function (component: TransportProfileMap) {
        let model: BusStop = component.ctxMenuMarker.curMarker.info;
        component.dialogService.openWindow('New bus stop', '', '50%', BusStopForm, {
            profileId: component.profileId,
            model: model
        }).subscribe((res: boolean) => {
            if (res) {
                component.loadBusStops();
            }
        });
    }
    fnDeleteBusStop = function (component: TransportProfileMap) {
        let model: BusStop = component.ctxMenuMarker.curMarker.info;
        component.dataService.deleteById<BusStop>(model.id, ModelClass.BUS_STOP)
            .then(() => {
                component.loadBusStops();
            });
    }
    // ========================= navigation ===================================
    switchSideNavContent<T extends SwitchedContent>(t: Type<T>, data: any) {
        var component = this;
        setTimeout(function () {
            component.changeTemplate(t, data);
        });
    }
    // ========================= leaflet private ==============================
    private createContextMenu(width: number, items: CtxMenuItem[]): any {
        let _ctxMenu = L.popup({
            minWidth: width,
            maxWidth: width,
            className: 'l-ctx-menu',
            closeButton: false,
            closeOnClick: false
        })
        var container = L.DomUtil.create('div');
        var _map = this.map;
        items.forEach((item: CtxMenuItem) => {
            let btn = this.createContextMenuBtn(container, item);
            L.DomEvent.on(btn, 'click', function () {
                _map.closePopup();
                item.onclick(item.component);
            });
        });
        _ctxMenu.setContent(container);
        console.log('context menu init complete');
        return _ctxMenu;
    }
    private createIcon(icName: string) {
        return L.icon({
            iconUrl: '/assets/image/' + icName + '.png',
            shadowUrl: '/assets/image/shadow.png',
            iconSize: [24, 27],
            shadowSize: [39, 27],
            iconAnchor: [12, 27],
            shadowAnchor: [12, 27],
            popupAnchor: [0, 0]
        });
    }
    private calcDistance(bs1: BusStop, bs2: BusStop) {
        let dLat: number = (bs2.latitude - bs1.latitude) * Math.PI / 180;
        let dLon: number = (bs2.longitude - bs1.longitude) * Math.PI / 180;
        let rLat1: number = (bs1.latitude) * Math.PI / 180;
        let rLat2: number = (bs2.latitude) * Math.PI / 180;
        let a: number = Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.sin(dLon / 2) * Math.sin(dLon / 2)
            * Math.cos(rLat1) * Math.cos(rLat2);
        let c: number = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return this.EARTH_RAD * c;
    }
    private calcWayDistance(way: BusStop[]) {
        let sum = 0;
        let prev: BusStop = null;
        way.forEach(bs => {
            if (prev == null) {
                prev = bs;
            } else {
                if (this.MOCK_BS != bs.name) {
                    let dist = this.calcDistance(bs, prev);
                    sum += dist;
                    prev = bs;
                }
            }
        });
        return sum;
    }
    private createMap(profile: TransportProfile, container: ElementRef) {
        var map = L.map.Sonya(container.nativeElement, {
            southWest: L.latLng(profile.southWestLat, profile.southWestLon),
            northEast: L.latLng(profile.northEastLat, profile.northEastLon),
            bounds: L.latLngBounds(L.latLng(profile.southWestLat, profile.southWestLon),
                L.latLng(profile.northEastLat, profile.northEastLon)),
            maxBounds: L.latLngBounds(L.latLng(profile.southWestLat, profile.southWestLon),
                L.latLng(profile.northEastLat, profile.northEastLon)),
            minZoom: profile.minZoom,
        }, profile.centerLat, profile.centerLon, profile.initialZoom);
        this.createLayer().addTo(map);
        container.nativeElement.style.height = (window.innerHeight) + 'px';
        map.invalidateSize(true);

        this.map = map;

        // map listeners
        var comp = this;
        var _map = this.map;
        map.on('click', function (e: any) {
            // prevent control clicks
            if (e.originalEvent && e.originalEvent.currentTarget
                && e.originalEvent.currentTarget
                && e.originalEvent.path) {
                // prev target
                var idx = e.originalEvent.path.indexOf(e.originalEvent.currentTarget) - 1;
                if (idx > 0 && e.originalEvent.path[idx]
                    && e.originalEvent.path[idx].className === 'leaflet-control-container') {
                    return;
                }
            }
            if (comp.ctxMenu) {
                if (comp.ctxMenu.isOpen()) {
                    _map.closePopup();
                } else {
                    comp.ctxMenu.setLatLng(e.latlng);
                    comp.ctxMenu.openOn(_map);
                    comp.setCurrentCoordinates(e.latlng);
                }
            }
        });
        map.on('zoomend', function (e) {
            comp.mapInteract();
        });
        map.on('dragend', function (e) {
            comp.mapInteract();
        });

        this.layerRouting.addTo(this.map);
    }
    private createLayer(): any {
        return L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
            id: 'osm.default'
        });
    }
    private updateBusStopLayer(busstops: BusStop[]): void {
        var start = (new Date()).getTime();
        this.layerBusStop.clearLayers();
        console.log('total bus stops [' + busstops.length + ']');
        if (busstops.length === 0) {
            return;
        }
        var arrayElements = [];
        for (var i = 0; i < busstops.length; i++) {
            var m = this.createMarker(busstops[i]);
            arrayElements.push(m);
        }
        this.layerBusStop = L.layerGroup(arrayElements).addTo(this.map);
        console.log('draw bus stop layer elapsed time [' + ((new Date()).getTime() - start) + '] ms');
    }
    private changeTemplate<T extends SwitchedContent>(t: Type<T>, data: any) {
        var component = this;
        let componentFactory = component.resolver.resolveComponentFactory(t);
        let viewContainerRef = component.sideNavTmpl.viewContainerRef;
        if (viewContainerRef.length == 0) {
            let componentRef = viewContainerRef.createComponent<T>(componentFactory);
            componentRef.instance.setData(data);
            component.viewInstance = componentRef.instance;
        }
    }
    private isBusStopGrid() {
        return this.viewInstance && this.viewInstance instanceof BusStopGrid;
    }
    private createContextMenuBtn(container: any, item: CtxMenuItem): any {
        var btn = L.DomUtil.create('button', '', container);
        btn.setAttribute('type', 'button');
        btn.className = "l-context-menu-button";
        btn.innerHTML = '<i class="material-icons">'
            + item.icon + '</i> <span>' + item.label + '</span>';
        return btn;
    }
    private setCurrentCoordinates(coords: any): void {
        this.coords = coords;
    }
    private createMarker(bs: BusStop): any {
        var marker = L.marker(new L.LatLng(bs.latitude, bs.longitude), {
            icon: bs.name === this.MOCK_BS
                ? this.createIcon('busstop_mock') : this.createIcon('busstop'),
            clickable: true,
            draggable: true,
            title: bs.name
        });
        marker.info = bs;
        var _comp = this;
        marker.on('contextmenu', function (e: any) {
            if (!_comp.isBusStopGrid()) {
                _comp.coords = e.latlng;
                _comp.ctxMenuMarker.setLatLng(e.latlng);
                _comp.ctxMenuMarker.openOn(_comp.map);
                _comp.ctxMenuMarker.curMarker = e.target;
            }
        });
        marker.on('dragend', function (e: any) {
            var selMarker = e.target;
            var model = selMarker.info
            _comp.moveBusStop(model, selMarker.getLatLng().lat, selMarker.getLatLng().lng);
        });
        marker.on('click', function (e: any) {
            if (_comp.isBusStopGrid()) {
                _comp.appendMarkerToRoute(bs);
            }
        });
        return marker;
    }
    private appendMarkerToRoute(markerBs: BusStop) {
        let bsGrid: BusStopGrid = <BusStopGrid> this.viewInstance;
        let exist: BusStop[] = bsGrid.busstops.filter((bs: BusStop) => bs.id === markerBs.id);
        if (exist.length === 0) {
            console.log('add bs to list');
            if (bsGrid.busstops.length < 2) {
                bsGrid.busstops.push(markerBs);
            } else {
                let closestDist = Number.MAX_VALUE;
                let copyArr: BusStop[] = [];
                let idx = 0;
                let counter = 0;
                bsGrid.busstops.forEach((bs) => {
                    copyArr.push(bs);
                    let dist = this.calcDistance(markerBs, bs);
                    if (dist < closestDist) {
                        closestDist = dist;
                        idx = counter;
                    }
                    counter++;
                });
                copyArr.splice((idx + 1), 0, markerBs);
                bsGrid.busstops.splice((idx), 0, markerBs);
                if (this.calcWayDistance(copyArr) < this.calcWayDistance(bsGrid.busstops)) {
                    bsGrid.busstops = copyArr;
                }
            }
        } else {
            console.log('remove bs from list');
            bsGrid.busstops = bsGrid.busstops.filter(bs => bs.id != markerBs.id);
        }
        let routeSettings: RouteProfile = bsGrid.path.route.type;
        this.drawRoute(bsGrid.busstops, routeSettings);
    }
    private mapInteract() {
        let bounds = this.map.getBounds();
        let totalMarkers: BusStop[] = this.calcBounds(this.allBusStops, bounds);
        if (totalMarkers.length < 200) { // no more then 200 bus stops, performance!
            this.updateBusStopLayer(totalMarkers);
        } else {
            this.updateBusStopLayer([]);
        }
    };
    private calcBounds(markers: BusStop[], bounds: any) {
        var p = {
            NElat: bounds._northEast.lat,
            SWlat: bounds._southWest.lat,
            NElng: bounds._northEast.lng,
            SWlng: bounds._southWest.lng
        };
        var rest: BusStop[] = [];
        for (var i = 0; i < markers.length; i++) {
            var lat = markers[i].latitude;
            var lon = markers[i].longitude;
            if (lat > p.SWlat && lat < p.NElat && lon > p.SWlng && lon < p.NElng) {
                rest.push(markers[i]);
            }
        }
        return rest;
    }
    private fitBoundsByMarkers(markers: BusStop[], paddingX: number) {
        if (markers.length === 0) {
            return;
        }
        this.map.fitBounds(this.getMarkersBounds(markers), {
            animate: true,
            duration: 1,
            easeLinearity: 0.25,
            paddingTopLeft: [paddingX ? paddingX : 0, 0]
        });
    }
    private getMarkersBounds(markers: BusStop[]) {
        var _sw_lat;
        var _sw_lon;
        var _ne_lat;
        var _ne_lon;
        var _delta = 0.003;
        for (var i = 0; i < markers.length; i++) {
            var m = markers[i];
            var lat = m.latitude;
            var lon = m.longitude;
            lon = lon ? lon : m.longitude;
            if (!_sw_lat || _sw_lat > lat) {
                _sw_lat = lat;
            }
            if (!_sw_lon || _sw_lon > lon) {
                _sw_lon = lon;
            }
            if (!_ne_lat || _ne_lat < lat) {
                _ne_lat = lat;
            }
            if (!_ne_lon || _ne_lon < lon) {
                _ne_lon = lon;
            }
        }
        var bounds = [
            [_sw_lat - _delta, _sw_lon - _delta],
            [_ne_lat + _delta, _ne_lon + _delta]
        ];
        return bounds;
    }
}


L.Map.Sonya = L.Map.extend({
    options: {
        zoomControl: false,
        keyboard: false,
        attributionControl: false
    },
    initialize: function (id: number, options: any, lat: number, lon: number, zoom: number) {
        L.Util.setOptions(this, options);
        L.Map.prototype.initialize.call(this, id, options);
        this.setView([lat, lon], zoom);
    }
});
L.map.Sonya = function (id: number, options: any, lat: number, lon: number, zoom: number) {
    return new L.Map.Sonya(id, options, lat, lon, zoom);
};
