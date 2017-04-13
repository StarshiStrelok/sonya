import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { NumberfieldComponent } from './numberfield.component';

describe('NumberfieldComponent', () => {
  let component: NumberfieldComponent;
  let fixture: ComponentFixture<NumberfieldComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ NumberfieldComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NumberfieldComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
