import {Component, HostBinding} from '@angular/core';
import {Router} from '@angular/router';
import {animate, state, style, transition, trigger} from '@angular/animations';
import {TranslateService} from '@ngx-translate/core';

import {Links} from './links';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styles: [`.app-toolbar {
                min-height: 50px;
                height: 50px;
                position: relative;
                z-index: 1;
            }
            .lang-menu-icon {
                vertical-align: middle;
            }
            `]
})
export class AppComponent {
    constructor(private translate: TranslateService, private router: Router) {
        // this language will be used as a fallback when a translation isn't found in the current language
        translate.setDefaultLang('en');
        // the lang to use, if the lang isn't available, it will use the current loader to get them
        translate.use('en');
        console.log('browser language [' + this.translate.getBrowserLang() + ']');
    }
    options = {
        position: ["top", "right"],
        timeOut: 5000,
        maxStack: 4
    };
    toControl() {
        this.router.navigate([Links.PROFILE_LIST]);
    }
    toMain() {
        this.router.navigate(['']);
    }
    changeLanguage(lang: string) {
        this.translate.use(lang);
    }
}

export class AnimatedSlide {
    @HostBinding('@routeAnimation') routeAnimation = true;
    @HostBinding('style.display') display = 'block';
    @HostBinding('style.position') position = 'absolute';
    @HostBinding('style.left') left = '0px';
    @HostBinding('style.right') right = '0px';
}

export const slideAnimation: any =
    trigger('routeAnimation', [
        state('*',
            style({
                opacity: 1,
                transform: 'translateX(0)'
            })
        ),
        transition(':enter', [
            style({
                opacity: 0,
                transform: 'translateX(-100%)'
            }),
            animate('0.5s ease-in')
        ]),
        transition(':leave', [
            animate('0.4s ease-out', style({
                opacity: 0,
                transform: 'translateX(100%)'
            }))
        ])
    ]);
