import {Component, OnInit} from '@angular/core'
import {Router} from '@angular/router';
import {Hero} from './hero';
import {HeroService} from './services/hero.service'

@Component({
    selector: 'my-heroes',
    templateUrl: './heroes.component.html',
    styleUrls: ['./heroes.component.css']
})
export class HeroesComponent implements OnInit {
    constructor(private heroService: HeroService, private router: Router) {}
    ngOnInit(): void {
        this.getHeroes();
    }
    heroes: Hero[];
    selectedHero: Hero;
    onSelect(hero: Hero): void {
        this.selectedHero = hero;
    }
    getHeroes(): void {
        this.heroService.getHeroesSlowly().then(value => this.heroes = value);
    }
    gotoDetail(): void {
        this.router.navigate(['/detail', this.selectedHero.id]);
    }
}
