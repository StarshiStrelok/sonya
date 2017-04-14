"use strict";
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var core_1 = require("@angular/core");
var forms_1 = require("@angular/forms");
var value_accessor_base_1 = require("../../common/value-accessor-base");
var TextfieldComponent = TextfieldComponent_1 = (function (_super) {
    __extends(TextfieldComponent, _super);
    function TextfieldComponent() {
        var _this = _super !== null && _super.apply(this, arguments) || this;
        _this.id = "textfield-" + (Math.random() * (100000 - 1) + 1).toFixed(0);
        return _this;
    }
    return TextfieldComponent;
}(value_accessor_base_1.ValueAccessorBase));
__decorate([
    core_1.Input()
], TextfieldComponent.prototype, "label");
__decorate([
    core_1.Input()
], TextfieldComponent.prototype, "control");
TextfieldComponent = TextfieldComponent_1 = __decorate([
    core_1.Component({
        selector: 'textfield',
        templateUrl: './textfield.component.html',
        styleUrls: ['./textfield.component.css'],
        providers: [{
                provide: forms_1.NG_VALUE_ACCESSOR,
                useExisting: core_1.forwardRef(function () { return TextfieldComponent_1; }),
                multi: true
            }]
    })
], TextfieldComponent);
exports.TextfieldComponent = TextfieldComponent;
var TextfieldComponent_1;
