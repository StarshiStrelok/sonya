import {Component, HostBinding, HostListener} from '@angular/core';
import {Router} from '@angular/router';
import {animate, state, style, transition, trigger} from '@angular/animations';
import {TranslateService} from '@ngx-translate/core';
import {GAService, EventCategory, EventAction} from './service/ga.service';
import {DialogService} from './service/dialog.service';
import {SecurityService} from './service/security.service';
import {RegistrationForm} from './security/registration.form';
import {StatFilterPanel} from './security/stat.filter.panel';

import {Links} from './links';
import {CookieService, CookieKey} from './service/cookie.service';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css']
})
export class AppComponent {
    isControlVisible = false;
    themes: any = [];
    activeTheme: any;
    @HostListener('window:keydown', ['$event'])
    showControl(event: KeyboardEvent) {
        if (event.keyCode === 9) {  // TAB
            this.isControlVisible = !this.isControlVisible;
        } else if (event.altKey === true && event.keyCode === 83) {     // Alt+S
            this.openRequestStatistic();
        }
    }
    constructor(
        private translate: TranslateService,
        private router: Router,
        private cookieService: CookieService,
        private dialogService: DialogService,
        private securityService: SecurityService,
        private ga: GAService
    ) {
        this.initSkins();
        // this language will be used as a fallback when a translation isn't found in the current language
        translate.setDefaultLang('en');
        // the lang to use, if the lang isn't available, it will use the current loader to get them
        let lang = this.cookieService.getCookie(CookieKey.LANG);
        lang = lang ? lang : this.translate.getBrowserLang();
        console.log('language [' + lang + ']');
        translate.use(lang);
    }
    // notifications
    options = {
        position: ["top", "right"],
        timeOut: 5000,
        maxStack: 4
    };
    private toControl() {
        this.router.navigate([Links.PROFILE_LIST]);
    }
    private initSkins() {
        // skin
        this.themes = [{
            id: 'deeppurple-amber',
            color: '#673ab7'
        }, {
            id: 'indigo-pink',
            color: '#3f51b5'
        }, {
            id: 'purple-green',
            color: '#7b1fa2'
        }, {
            id: 'pink-bluegrey',
            color: '#c2185b'
        }]
        let skin = this.cookieService.getCookie(CookieKey.SKIN);
        for (let theme of this.themes) {
            if (theme.id === skin) {
                this.activeTheme = theme;
                break;
            }
        }
    }
    signIn() {
        this.securityService.profilesCount().then(count => {
            if (Number(count) === 0) {
                this.dialogService.openWindow('Please, register new admin user',
                    '', '50%', RegistrationForm, {
                        isRegistration: true
                    })
                    .subscribe((res: boolean) => {
                        if (res) {
                            this.signInDialogOpen();
                        }
                    });
            } else {
                this.signInDialogOpen();
            }
        });
    }
    openRequestStatistic() {
        this.dialogService.openWindow('StatFilter',
            '', '50%', StatFilterPanel, {})
    }
    private signInDialogOpen() {
        this.dialogService.openWindow('Sign In',
            '', '50%', RegistrationForm, {
                isRegistration: false
            }).subscribe((res: boolean) => {
                if (res) {
                    this.toControl();
                }
            });
    }
    toMain() {
        this.router.navigate(['']);
    }
    changeLanguage(lang: string) {
        this.translate.use(lang);
        this.cookieService.setCookie(CookieKey.LANG, lang);
        this.ga.sendEvent(EventCategory.INTERFACE, EventAction.SWITCH_LANG);
    }
    isMobile(): boolean {
        return window.innerWidth <= 600;
    }
    changeSkin(skin: any) {
        console.log('apply skin [' + skin.id + ']');
        var oldlink = document.getElementsByTagName("link").item(0);
        var newlink = document.createElement("link");
        newlink.setAttribute("rel", "stylesheet");
        newlink.setAttribute("type", "text/css");
        newlink.setAttribute("href", 'assets/skins/' + skin.id + '.css');
        this.cookieService.setCookie(CookieKey.SKIN, skin.id);
        this.activeTheme = skin;
        setTimeout(function () {
            document.getElementsByTagName("head").item(0).replaceChild(newlink, oldlink);
        }, 1000);
        this.ga.sendEvent(EventCategory.INTERFACE, EventAction.SWITCH_THEME);
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
