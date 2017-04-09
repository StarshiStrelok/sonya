import { HeroPage } from './app.po';

describe('hero App', () => {
  let page: HeroPage;

  beforeEach(() => {
    page = new HeroPage();
  });

  it('should display message saying app works', () => {
    page.navigateTo();
    expect(page.getParagraphText()).toEqual('app works!');
  });
});
