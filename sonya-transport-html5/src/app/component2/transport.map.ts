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
import {MdSidenav} from '@angular/material';
import {NotificationsService} from 'angular2-notifications';
import {TranslateService} from '@ngx-translate/core';

import {slideAnimation, AnimatedSlide} from './../app.component';
import {TransportProfile, ModelClass, MapLayer} from '../model/abs.model';
import {DataService} from '../service/data.service';
import {CookieService, CookieKey} from '../service/cookie.service';
import {CtxMenuItem} from '../model/ctx.menu.item';
import {GeoCoder} from './geocoder';
import {SearchTab} from './search.tab';
import {Waiting} from '../lib/material/waiting';
import {OSRMService} from '../service/osrm.service';
import {EndpointLayer} from './endpoint.layer';

declare var L: any;

export class SidenavItem {
    public static SEARCH = 'item-search';
    public static SCHEDULE = 'item-schedule';
}

@Component({
    selector: 'transport-map',
    templateUrl: './transport.map.html',
    styleUrls: ['./transport.map.css'],
    animations: [slideAnimation]
})
export class TransportMap extends AnimatedSlide implements OnInit {
    MOCK_BS: string = 'mock';
    @ViewChild('map') mapElement: ElementRef;
    @ViewChild('sidenav') sideNav: MdSidenav;
    @ViewChild(GeoCoder) geocoder: GeoCoder;
    @ViewChild(SearchTab) searchTabs: SearchTab;
    @ViewChild(Waiting) waiting: Waiting;
    map: any;
    mapMenu: any
    profiles: TransportProfile[];
    activeProfile: TransportProfile;
    layerEndpoint = new EndpointLayer();
    isMenuOpen: boolean = !this.isMobile();
    component: TransportMap = this;
    activeMapLayer: MapLayer;
    private activeLeafletLayer: any;
    private currentSidenavItem: SidenavItem = SidenavItem.SEARCH;
    constructor(
        public dataService: DataService,
        public notificationService: NotificationsService,
        public osrmService: OSRMService,
        public translate: TranslateService,
        private cookieService: CookieService
    ) {
        super();
    }
    ngOnInit() {
        this.initProfile();
    }
    createContextMenu() {
        this.translate.get('transport-map.map.ctx-menu.start-point').subscribe(val => {
            this.mapMenu = this.createMapMenu(180, [
                new CtxMenuItem('A', val,
                    this.fnMakeStartPoint, this),
                new CtxMenuItem('B', this.translate.instant('transport-map.map.ctx-menu.end-point'),
                    this.fnMakeEndPoint, this)
            ]);
        });
    }
    fnMakeStartPoint(comp: TransportMap) {
        comp.setEndpoint(true);
    }
    fnMakeEndPoint(comp: TransportMap) {
        comp.setEndpoint(false);
    }
    setEndpoint(isStart: boolean) {
        let ll = this.mapMenu.getLatLng();
        if (isStart) {
            this.layerEndpoint.showStartMarker(ll.lat, ll.lng, false);
        } else {
            this.layerEndpoint.showEndMarker(ll.lat, ll.lng, false);
        }
        this.geocoder.reverseSearch(isStart, ll.lat, ll.lng);
    }
    openMenu() {
        this.currentSidenavItem = SidenavItem.SEARCH;
        this.isMenuOpen = true;
        this.sideNav.open().then(res => this.map.invalidateSize(true));
    }
    openMenu2() {
        this.currentSidenavItem = SidenavItem.SCHEDULE;
        this.isMenuOpen = true;
        this.sideNav.open().then(res => this.map.invalidateSize(true));
    }
    closeMenu() {
        this.isMenuOpen = false;
        this.sideNav.close().then(res => this.map.invalidateSize(true));
    }
    isMobile(): boolean {
        return window.innerWidth <= 600;
    }
    changeMapLayer(event: number) {
        console.log('new map layer index [' + event + ']');
        this.activeMapLayer = this.activeProfile.mapLayers[event];
        this.setMapLayer();
        this.cookieService.setCookie(CookieKey.MAP + this.activeProfile.id, this.activeMapLayer.id);
    }
    switchProfile(profile: TransportProfile) {
        this.cookieService.setCookie(CookieKey.PROFILE, profile.id);
        this.geocoder.clearStart();
        this.geocoder.clearEnd();
        this.initProfile();
    }
    readUserProfile() {
        let pid = Number(this.cookieService.getCookie(CookieKey.PROFILE));
        if (pid) {
            let p: TransportProfile[] = this.profiles.filter(profile => profile.id === pid);
            if (p.length == 1) {
                this.activeProfile = p[0];
            } else {
                this.activeProfile = this.profiles[0];
            }
        } else {
            this.activeProfile = this.profiles[0];
        }
    }
    private initProfile() {
        if (this.map) {
            this.map.remove();
        }
        this.translate.onLangChange.subscribe((event: any) => {
            this.createContextMenu();
        });
        this.dataService.getAll<TransportProfile>(ModelClass.TRANSPORT_PROFILE)
            .then((profiles: TransportProfile[]) => {
                this.profiles = profiles;
                this.readUserProfile();
                this.createMap(this.activeProfile);
                this.createContextMenu();
            });
    }
    private createMap(profile: TransportProfile) {
        var map = L.map.Sonya(this.mapElement.nativeElement, {
            southWest: L.latLng(profile.southWestLat, profile.southWestLon),
            northEast: L.latLng(profile.northEastLat, profile.northEastLon),
            bounds: L.latLngBounds(L.latLng(profile.southWestLat, profile.southWestLon),
                L.latLng(profile.northEastLat, profile.northEastLon)),
            maxBounds: L.latLngBounds(L.latLng(profile.southWestLat, profile.southWestLon),
                L.latLng(profile.northEastLat, profile.northEastLon)),
            minZoom: profile.minZoom,
        }, profile.centerLat, profile.centerLon, profile.initialZoom);
        this.mapElement.nativeElement.style.height = (window.innerHeight - 50) + 'px';
        map.invalidateSize(true);

        let comp = this;
        map.on('click', function (e: any) {
            if (comp.mapMenu) {
                if (comp.mapMenu.isOpen()) {
                    map.closePopup();
                } else {
                    comp.mapMenu.setLatLng(e.latlng);
                    comp.mapMenu.openOn(map);
                }
            }
        });
        this.map = map;
        this.activeMapLayer = this.getStartMapLayer();
        this.setMapLayer();
        this.layerEndpoint.init(this.map, this);
    }
    private setMapLayer() {
        if (this.activeLeafletLayer) {
            this.map.removeLayer(this.activeLeafletLayer);
        }
        let _layer = L.tileLayer(this.activeMapLayer.url
            + (this.activeMapLayer.url.indexOf('access_token=') !== -1
                ? this.activeProfile.mapboxKey : ''), {
                id: this.activeMapLayer.id
            });
        _layer.addTo(this.map);
        this.activeLeafletLayer = _layer;
    }
    private createMapMenu(width: number, items: CtxMenuItem[]): any {
        let _mapMenu = L.popup({
            minWidth: width,
            maxWidth: width,
            className: 'l-ctx-menu l-ctx-menu-offset',
            offset: [90, 84],
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
        _mapMenu.setContent(container);
        return _mapMenu;
    }
    private createContextMenuBtn(container: any, item: CtxMenuItem): any {
        var btn = L.DomUtil.create('button', '', container);
        btn.setAttribute('type', 'button');
        btn.className = "l-context-menu-button";
        btn.innerHTML = '<span class="' + (item.icon === 'A' ? 'letter-a' : 'letter-b') + '">'
            + item.icon + '</span>'
            + '<span class> ' + item.label + '</span>';
        return btn;
    }
    private getStartMapLayer(): MapLayer {
        if (this.activeProfile.mapLayers && this.activeProfile.mapLayers.length > 0) {
            let layerC = this.cookieService.getCookie(CookieKey.MAP + this.activeProfile.id);
            if (layerC) {
                let layerId = Number(layerC);
                let f: MapLayer[] = this.activeProfile.mapLayers.filter(l => l.id === layerId);
                if (f.length === 1) {
                    return f[0];
                } else {
                    return this.activeProfile.mapLayers[0];
                }
            } else {
                return this.activeProfile.mapLayers[0];
            }
        } else {
            return new MapLayer(-1, 'OSM', 'http://{s}.tile.osm.org/{z}/{x}/{y}.png', 'layers');
        }
    }
}
