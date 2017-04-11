"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var core_1 = require("@angular/core");
var NumberComponent = (function () {
    function NumberComponent() {
        this.fieldChange = new core_1.EventEmitter();
    }
    NumberComponent.prototype.ngOnInit = function () {
    };
    NumberComponent.prototype.change = function (newValue) {
        this.field = newValue;
        this.fieldChange.emit(newValue);
    };
    return NumberComponent;
}());
__decorate([
    core_1.Input()
], NumberComponent.prototype, "label");
__decorate([
    core_1.Input()
], NumberComponent.prototype, "field");
__decorate([
    core_1.Output()
], NumberComponent.prototype, "fieldChange");
__decorate([
    core_1.Input()
], NumberComponent.prototype, "required");
__decorate([
    core_1.Input()
], NumberComponent.prototype, "max");
__decorate([
    core_1.Input()
], NumberComponent.prototype, "min");
NumberComponent = __decorate([
    core_1.Component({
        selector: 's-input-number',
        templateUrl: './number.component.html',
        styleUrls: ['./number.component.css']
    })
], NumberComponent);
exports.NumberComponent = NumberComponent;
