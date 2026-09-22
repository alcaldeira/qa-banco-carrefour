import { expect, Locator, Page } from '@playwright/test';
import { BasePage } from './BasePage';

export class CheckoutPage extends BasePage {
  readonly firstName: Locator;
  readonly lastName: Locator;
  readonly postalCode: Locator;
  readonly continueButton: Locator;
  readonly finishButton: Locator;
  readonly completeHeader: Locator;
  readonly overviewTitle: Locator;
  readonly errorMessage: Locator;

  constructor(page: Page) {
    super(page);
    this.firstName = page.locator('[data-test="firstName"]');
    this.lastName = page.locator('[data-test="lastName"]');
    this.postalCode = page.locator('[data-test="postalCode"]');
    this.continueButton = page.locator('[data-test="continue"]');
    this.finishButton = page.locator('[data-test="finish"]');
    this.completeHeader = page.locator('.complete-header');
    this.overviewTitle = page.locator('.title');
    this.errorMessage = page.locator('[data-test="error"]');
  }

  async fillCustomerInfo(firstName: string, lastName: string, postalCode: string): Promise<void> {
    await this.fillField(this.firstName, firstName);
    await this.fillField(this.lastName, lastName);
    await this.fillField(this.postalCode, postalCode);
  }

  async continue(): Promise<void> {
    await this.click(this.continueButton);
  }

  async finish(): Promise<void> {
    await this.click(this.finishButton);
  }

  async expectOverviewVisible(): Promise<void> {
    await this.waitForVisible(this.overviewTitle);
  }

  async expectCustomerInfoVisible(): Promise<void> {
    await this.waitForVisible(this.overviewTitle);
    await expect(this.overviewTitle).toHaveText('Checkout: Your Information');
  }

  async expectOrderSuccess(): Promise<void> {
    await this.waitForVisible(this.completeHeader);
  }
}
