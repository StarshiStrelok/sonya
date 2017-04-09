import {Component, OnInit} from '@angular/core';
import {HeroService} from '../services/hero.service';
import {Hero} from '../hero';

@Component({
    selector: 'my-dashboard',
    templateUrl: './dashboard.component.html',
    styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
    constructor(private heroService: HeroService) {}
    ngOnInit() {
        this.heroService.getHeroes().then(heroes => this.heroes = heroes.slice(1, 5));
    }
    heroes: Hero[] = [];
}
