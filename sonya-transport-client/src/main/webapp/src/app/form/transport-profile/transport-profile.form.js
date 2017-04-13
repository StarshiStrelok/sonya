"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var core_1 = require("@angular/core");
var forms_1 = require("@angular/forms");
var TransportProfileFormComponent = (function () {
    function TransportProfileFormComponent(fb) {
        this.fb = fb;
        this.createForm();
    }
    TransportProfileFormComponent.prototype.createForm = function () {
        this.transportProfileForm = this.fb.group({
            name: ['', [forms_1.Validators.required, forms_1.Validators.maxLength(100)]],
            southWestLat: [''],
            southWestLon: [''],
            northEastLat: [''],
            northEastLon: ['']
        });
    };
    return TransportProfileFormComponent;
}());
TransportProfileFormComponent = __decorate([
    core_1.Component({
        selector: 'transport-profile-form-r',
        templateUrl: './transport-profile-form.component.html',
        styleUrls: ['./transport-profile-form.component.css']
    })
], TransportProfileFormComponent);
exports.TransportProfileFormComponent = TransportProfileFormComponent;
