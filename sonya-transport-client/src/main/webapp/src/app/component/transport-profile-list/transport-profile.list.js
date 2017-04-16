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
"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var core_1 = require("@angular/core");
var abs_model_1 = require("../../model/abs.model");
var route_module_1 = require("../../route.module");
var TransportProfileList = (function () {
    function TransportProfileList(dataService, dialogService, router) {
        this.dataService = dataService;
        this.dialogService = dialogService;
        this.router = router;
    }
    ;
    TransportProfileList.prototype.ngOnInit = function () {
        var _this = this;
        this.dataService.getAll(abs_model_1.ModelClass.TRANSPORT_PROFILE)
            .then(function (profiles) { return _this.profiles = profiles; });
    };
    TransportProfileList.prototype.newProfile = function () {
        this.router.navigate([route_module_1.Links.PROFILE_FORM]);
    };
    TransportProfileList.prototype.editProfile = function (id) {
        this.router.navigate([route_module_1.Links.PROFILE_FORM, id]);
    };
    TransportProfileList.prototype.deleteProfile = function (id) {
        console.log('delete profile [' + id + ']');
        this.dialogService.confirm('Delete profile', 'Are you sure that you want delete this profile?')
            .subscribe(function (result) { return console.log('Confirm result [' + result + ']'); });
    };
    TransportProfileList.prototype.openMap = function (id) {
        console.log('open profile map [' + id + ']');
    };
    return TransportProfileList;
}());
TransportProfileList = __decorate([
    core_1.Component({
        selector: 'transport-profile-list',
        templateUrl: './transport-profile.list.html'
    })
], TransportProfileList);
exports.TransportProfileList = TransportProfileList;
