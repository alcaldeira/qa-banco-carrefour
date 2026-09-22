import { expect, Locator, Page } from '@playwright/test';
import { BasePage } from './BasePage';

export class LoginPage extends BasePage {
  readonly usernameInput: Locator;
  readonly passwordInput: Locator;
  readonly loginButton: Locator;
  readonly errorMessage: Locator;
  readonly appLogo: Locator;

  constructor(page: Page) {
    super(page);
    this.usernameInput = page.locator('[data-test="username"]');
    this.passwordInput = page.locator('[data-test="password"]');
    this.loginButton = page.locator('[data-test="login-button"]');
    this.errorMessage = page.locator('[data-test="error"]');
    this.appLogo = page.locator('.login_logo');
  }

  async open(): Promise<void> {
    await this.page.goto('/');
    await this.waitForVisible(this.appLogo);
  }

  async login(username: string, password: string): Promise<void> {
    await this.fillField(this.usernameInput, username);
    await this.fillField(this.passwordInput, password);
    await this.click(this.loginButton);
  }

  async expectErrorMessage(expectedMessage: string): Promise<void> {
    await this.waitForVisible(this.errorMessage);
    await expect(this.errorMessage).toHaveText(expectedMessage);
  }
}
