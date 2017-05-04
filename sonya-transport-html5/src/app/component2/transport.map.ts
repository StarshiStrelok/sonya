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

import {slideAnimation, AnimatedSlide} from './../app.component';
import {TransportProfile, ModelClass, BusStop} from '../model/abs.model';
import {DataService} from '../service/data.service';
import {CtxMenuItem} from '../model/ctx.menu.item';
import {GeoCoder} from './geocoder';

declare var L: any;

@Component({
    selector: 'transport-map',
    templateUrl: './transport.map.html',
    styleUrls: ['./transport.map.css'],
    animations: [slideAnimation]
})
export class TransportMap extends AnimatedSlide implements OnInit {
    @ViewChild('map') mapElement: ElementRef;
    @ViewChild('sidenav') sideNav: MdSidenav;
    @ViewChild(GeoCoder) geocoder: GeoCoder;
    map: any;
    mapMenu: any
    profiles: TransportProfile[];
    activeProfile: TransportProfile;
    layerEndpoint: EndpointLayer = new EndpointLayer();
    isMenuOpen: boolean = true;
    constructor(private dataService: DataService) {
        super();
    }

    ngOnInit() {
        this.dataService.getAll<TransportProfile>(ModelClass.TRANSPORT_PROFILE)
            .then((profiles: TransportProfile[]) => {
                this.profiles = profiles;
                this.activeProfile = this.profiles[0];
                this.createMap(this.activeProfile);
                this.mapMenu = this.createMapMenu(180, [
                    new CtxMenuItem('A', 'Start point',
                            this.fnMakeStartPoint, this),
                    new CtxMenuItem('B', 'End point',
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
        this.createLayer().addTo(map);
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
        this.layerEndpoint.init(this.map, this);
    }
    private createLayer(): any {
        return L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
            id: 'osm.default'
        });
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
}

export class EndpointLayer {
    layerEndpoint: any;
    startMarker: any;
    endMarker: any;
    map: any;
    parent: TransportMap;
    init(map: any, parent: TransportMap) {
        this.map = map;
        this.parent = parent;
        this.layerEndpoint = L.layerGroup([]);
        this.layerEndpoint.addTo(map);
        this.startMarker = this.createMarker(true);
        this.endMarker = this.createMarker(false);
    }
    showStartMarker(lat: number, lon: number, isMove: boolean) {
        this.startMarker.setLatLng(new L.LatLng(lat, lon));
        if (!this.layerEndpoint.hasLayer(this.startMarker)) {
            this.layerEndpoint.addLayer(this.startMarker);
        }
        if (isMove) {
            this.moveToMarker(lat, lon);
        }
        this.checkSearchConditions();
    }
    showEndMarker(lat: number, lon: number, isMove: boolean) {
        this.endMarker.setLatLng(new L.LatLng(lat, lon));
        if (!this.layerEndpoint.hasLayer(this.endMarker)) {
            this.layerEndpoint.addLayer(this.endMarker);
        }
        if (isMove) {
            this.moveToMarker(lat, lon);
        }
        this.checkSearchConditions();
    }
    hideStartMarker() {
        if (this.layerEndpoint.hasLayer(this.startMarker)) {
            this.layerEndpoint.removeLayer(this.startMarker);
        }
    }
    hideEndMarker() {
        if (this.layerEndpoint.hasLayer(this.endMarker)) {
            this.layerEndpoint.removeLayer(this.endMarker);
        }
    }
    private createMarker(isStart: boolean): any {
        var marker = L.marker(new L.LatLng(0, 0), {
            icon: isStart
                ? this.createIcon('start') : this.createIcon('end'),
            clickable: true,
            draggable: true,
            title: isStart ? 'Start' : 'Finish',
            isStart: isStart
        });
        var comp = this;
        marker.on('dragend', function (e: any) {
            let ll = marker.getLatLng();
            if (e.target.options.isStart) {
                comp.parent.geocoder.reverseSearch(true, ll.lat, ll.lng);
            } else {
                comp.parent.geocoder.reverseSearch(false, ll.lat, ll.lng);
            }
            comp.checkSearchConditions();
        });
        return marker;
    }
    private createIcon(icName: string) {
        return L.icon({
            iconUrl: '/assets/image/' + icName + '.png',
            shadowUrl: '/assets/image/shadow.png',
            iconSize: [32, 37],
            shadowSize: [39, 27],
            iconAnchor: [16, 37],
            shadowAnchor: [12, 27],
            popupAnchor: [0, 0]
        });
    }
    private moveToMarker(lat: number, lon: number) {
        this.map.flyTo(new L.LatLng(lat, lon), 16, {
            animate: true
        });
    }
    private checkSearchConditions() {
        if (this.layerEndpoint.hasLayer(this.startMarker)
            && this.layerEndpoint.hasLayer(this.endMarker)) {
            console.log('search');
        }
    }
}

export class SearchRoute {
    
}