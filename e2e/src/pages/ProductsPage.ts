import { Locator, Page } from '@playwright/test';
import { BasePage } from './BasePage';

export class ProductsPage extends BasePage {
  readonly title: Locator;
  readonly inventoryList: Locator;
  readonly shoppingCartBadge: Locator;
  readonly shoppingCartLink: Locator;
  readonly sortDropdown: Locator;
  readonly productNames: Locator;
  readonly addToCartButtons: Locator;

  constructor(page: Page) {
    super(page);
    this.title = page.locator('.title');
    this.inventoryList = page.locator('.inventory_item');
    this.shoppingCartBadge = page.locator('.shopping_cart_badge');
    this.shoppingCartLink = page.locator('.shopping_cart_link');
    this.sortDropdown = page.locator('[data-test="product-sort-container"]');
    this.productNames = page.locator('.inventory_item_name');
    this.addToCartButtons = page.locator('[data-test^="add-to-cart"]');
  }

  async expectLoaded(): Promise<void> {
    await this.waitForVisible(this.title);
    await this.page.locator('.inventory_list').waitFor({ state: 'visible' });
  }

  async addProductByName(productName: string): Promise<void> {
    const product = this.inventoryList.filter({ hasText: productName }).locator('button');
    await this.click(product);
  }

  async addBackpack(): Promise<void> {
    await this.addProductByName('Sauce Labs Backpack');
  }

  async openCart(): Promise<void> {
    await this.click(this.shoppingCartLink);
  }

  async sortByPriceHighToLow(): Promise<void> {
    await this.sortDropdown.selectOption('hilo');
  }

  async getProductNames(): Promise<string[]> {
    return await this.productNames.allTextContents();
  }

  async expectSortedByHighToLow(): Promise<void> {
    const prices = await this.page.locator('.inventory_item_price').allTextContents();
    const numericPrices = prices.map((value) => Number(value.replace('$', '')));
    const sorted = [...numericPrices].sort((a, b) => b - a);
    await this.page.waitForFunction(
      ([expected]) => {
        const elements = Array.from(document.querySelectorAll('.inventory_item_price'));
        const values = elements.map((el) => Number(el.textContent?.replace('$', '') ?? '0'));
        return JSON.stringify(values) === JSON.stringify(expected);
      },
      [sorted]
    );
  }
}
