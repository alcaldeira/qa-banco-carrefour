import { Locator, Page } from '@playwright/test';

export class BasePage {
  protected readonly page: Page;

  constructor(page: Page) {
    this.page = page;
  }

  protected async waitForVisible(locator: Locator, timeout = 15000): Promise<void> {
    await locator.waitFor({ state: 'visible', timeout });
  }

  protected async click(locator: Locator): Promise<void> {
    await this.waitForVisible(locator);
    await locator.click();
  }

  protected async fillField(locator: Locator, value: string): Promise<void> {
    await this.waitForVisible(locator);
    await locator.fill(value);
  }

  protected async getText(locator: Locator): Promise<string> {
    await this.waitForVisible(locator);
    return (await locator.textContent()) ?? '';
  }

  protected async clearAndFill(locator: Locator, value: string): Promise<void> {
    await this.waitForVisible(locator);
    await locator.fill(value);
  }
}
