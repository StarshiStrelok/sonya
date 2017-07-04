import {Component, HostBinding, HostListener} from '@angular/core';
import {Router} from '@angular/router';
import {animate, state, style, transition, trigger} from '@angular/animations';
import {TranslateService} from '@ngx-translate/core';
import {DialogService} from './service/dialog.service';
import {SecurityService} from './service/security.service';
import {RegistrationForm} from './security/registration.form';

import {Links} from './links';
import {CookieService, CookieKey} from './service/cookie.service';

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
            .title-text {
                font-weight: bold;
            }
            `]
})
export class AppComponent {
    isControlVisible = false;
    @HostListener('window:keydown', ['$event'])
    showControl(event: KeyboardEvent) {
        if (event.keyCode === 9) {  // TAB
            this.isControlVisible = !this.isControlVisible;
        }
    }
    constructor(
        private translate: TranslateService,
        private router: Router,
        private cookieService: CookieService,
        private dialogService: DialogService,
        private securityService: SecurityService
    ) {
        // this language will be used as a fallback when a translation isn't found in the current language
        translate.setDefaultLang('en');
        // the lang to use, if the lang isn't available, it will use the current loader to get them
        let lang = this.cookieService.getCookie(CookieKey.LANG);
        lang = lang ? lang : this.translate.getBrowserLang();
        console.log('language [' + lang + ']');
        translate.use(lang);
    }
    options = {
        position: ["top", "right"],
        timeOut: 5000,
        maxStack: 4
    };
    private toControl() {
        this.router.navigate([Links.PROFILE_LIST]);
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
    }
    isMobile(): boolean {
        return window.innerWidth <= 600;
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
