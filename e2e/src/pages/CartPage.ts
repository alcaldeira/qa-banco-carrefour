import { expect, Locator, Page } from '@playwright/test';
import { BasePage } from './BasePage';

export class CartPage extends BasePage {
  readonly pageTitle: Locator;
  readonly cartItems: Locator;
  readonly checkoutButton: Locator;
  readonly removeButtons: Locator;

  constructor(page: Page) {
    super(page);
    this.pageTitle = page.locator('.title');
    this.cartItems = page.locator('.cart_item');
    this.checkoutButton = page.locator('[data-test="checkout"]');
    this.removeButtons = page.locator('[data-test^="remove-"]');
  }

  async expectLoaded(): Promise<void> {
    await this.waitForVisible(this.pageTitle);
  }

  async proceedToCheckout(): Promise<void> {
    await this.click(this.checkoutButton);
  }

  async removeItemByProductName(productName: string): Promise<void> {
    const item = this.cartItems.filter({ hasText: productName });
    const removeButton = item.locator('button');
    await this.click(removeButton);
  }

  async expectItemQty(productName: string, expectedQuantity: number): Promise<void> {
    const item = this.cartItems.filter({ hasText: productName });
    await expect(item.locator('.cart_quantity')).toHaveText(String(expectedQuantity));
  }
}
