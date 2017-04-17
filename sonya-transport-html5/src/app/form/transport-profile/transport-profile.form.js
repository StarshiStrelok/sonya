"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var core_1 = require("@angular/core");
var forms_1 = require("@angular/forms");
var min_number_directive_1 = require("../../directive/validator/min-number.directive");
var max_number_directive_1 = require("../../directive/validator/max-number.directive");
var TransportProfileForm = (function () {
    function TransportProfileForm(fb) {
        this.fb = fb;
        this.createForm();
    }
    TransportProfileForm.prototype.createForm = function () {
        this.transportProfileForm = this.fb.group({
            name: ['', [forms_1.Validators.required, forms_1.Validators.maxLength(100)]],
            southWestLat: ['', [forms_1.Validators.required, min_number_directive_1.minNumberValidator(-90), max_number_directive_1.maxNumberValidator(90)]],
            southWestLon: ['', [forms_1.Validators.required, min_number_directive_1.minNumberValidator(-180), max_number_directive_1.maxNumberValidator(180)]],
            northEastLat: ['', [forms_1.Validators.required, min_number_directive_1.minNumberValidator(-90), max_number_directive_1.maxNumberValidator(90)]],
            northEastLon: ['', [forms_1.Validators.required, min_number_directive_1.minNumberValidator(-180), max_number_directive_1.maxNumberValidator(180)]],
            initialZoom: ['', [forms_1.Validators.required, min_number_directive_1.minNumberValidator(0), max_number_directive_1.maxNumberValidator(19)]],
            minZoom: ['', [forms_1.Validators.required, min_number_directive_1.minNumberValidator(0), max_number_directive_1.maxNumberValidator(19)]]
        });
    };
    return TransportProfileForm;
}());
TransportProfileForm = __decorate([
    core_1.Component({
        selector: 'transport-profile-form',
        templateUrl: './transport-profile.form.html'
    })
], TransportProfileForm);
exports.TransportProfileForm = TransportProfileForm;
