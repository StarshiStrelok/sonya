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
import {TransportProfile, ModelClass} from '../model/abs.model';
import {DataService} from '../service/data.service';
import {CtxMenuItem} from '../model/ctx.menu.item';

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
    map: any;
    mapMenu: any
    profiles: TransportProfile[];
    coordStart: any;
    coordEnd: any;
    isMenuOpen: boolean = true;
    constructor(private dataService: DataService) {
        super();
    }

    ngOnInit() {
        this.dataService.getAll<TransportProfile>(ModelClass.TRANSPORT_PROFILE)
            .then((profiles: TransportProfile[]) => {
                this.profiles = profiles;
                this.createMap(this.profiles[0]);
                this.mapMenu = this.createMapMenu(180, [
                    new CtxMenuItem('A', 'Start point',
                            this.fnMakeStartPoint, this),
                    new CtxMenuItem('B', 'End point',
                            this.fnMakeEndPoint, this)
                ]);
            });
    }
    fnMakeStartPoint(comp: TransportMap) {
        comp.search(true);
    }
    fnMakeEndPoint(comp: TransportMap) {
        comp.search(false);
    }
    search(isStart: boolean) {
        if (isStart) {
            this.coordStart = this.mapMenu.getLatLng();
        } else {
            this.coordEnd = this.mapMenu.getLatLng();
        }
        if (this.coordStart && this.coordEnd) {
            console.log('make search');
        }
    }
    openMenu() {
        this.isMenuOpen = true;
        this.sideNav.open().then(res => this.map.invalidateSize(true));
    }
    closeMenu() {
        this.isMenuOpen = false;
        this.sideNav.close().then(res => this.map.invalidateSize(true));
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
        this.mapElement.nativeElement.style.height = (window.innerHeight) + 'px';
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
