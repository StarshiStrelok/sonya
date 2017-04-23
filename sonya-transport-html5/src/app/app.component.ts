import {Component, HostBinding} from '@angular/core';
import {animate, state, style, transition, trigger} from '@angular/animations';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html'
})
export class AppComponent {

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
