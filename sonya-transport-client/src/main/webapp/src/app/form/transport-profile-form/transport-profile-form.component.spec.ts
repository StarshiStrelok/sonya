import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TransportProfileFormComponent } from './transport-profile-form.component';

describe('TransportProfileFormComponent', () => {
  let component: TransportProfileFormComponent;
  let fixture: ComponentFixture<TransportProfileFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TransportProfileFormComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TransportProfileFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
