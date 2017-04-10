import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TransportProfileComponent } from './transport-profile.component';

describe('TransportProfileComponent', () => {
  let component: TransportProfileComponent;
  let fixture: ComponentFixture<TransportProfileComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TransportProfileComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TransportProfileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
