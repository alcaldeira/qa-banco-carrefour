import { test } from '@playwright/test';
import { LoginPage } from '../src/pages/LoginPage';
import { ProductsPage } from '../src/pages/ProductsPage';
import { CartPage } from '../src/pages/CartPage';
import { CheckoutPage } from '../src/pages/CheckoutPage';
import { validUser, errorMessages } from '../src/data/users';

test.describe('Sauce Demo - professional automation suite', () => {
  test('Login with valid credentials', async ({ page }) => {
    const loginPage = new LoginPage(page);
    const productsPage = new ProductsPage(page);

    await loginPage.open();
    await loginPage.login(validUser.username, validUser.password);
    await productsPage.expectLoaded();
  });

  test('Login with invalid credentials shows error', async ({ page }) => {
    const loginPage = new LoginPage(page);

    await loginPage.open();
    await loginPage.login('locked_out_user', 'wrong-password');
    await loginPage.expectErrorMessage(errorMessages.loginMismatch);
  });

  test('Complete purchase flow', async ({ page }) => {
    const loginPage = new LoginPage(page);
    const productsPage = new ProductsPage(page);
    const cartPage = new CartPage(page);
    const checkoutPage = new CheckoutPage(page);

    await loginPage.open();
    await loginPage.login(validUser.username, validUser.password);
    await productsPage.expectLoaded();
    await productsPage.addBackpack();
    await productsPage.openCart();
    await cartPage.expectLoaded();
    await cartPage.proceedToCheckout();
    await checkoutPage.fillCustomerInfo('Anderson', 'Caldeira', '01000-000');
    await checkoutPage.continue();
    await checkoutPage.expectOverviewVisible();
    await checkoutPage.finish();
    await checkoutPage.expectOrderSuccess();
  });

  test('Remove item from cart', async ({ page }) => {
    const loginPage = new LoginPage(page);
    const productsPage = new ProductsPage(page);
    const cartPage = new CartPage(page);

    await loginPage.open();
    await loginPage.login(validUser.username, validUser.password);
    await productsPage.expectLoaded();
    await productsPage.addBackpack();
    await productsPage.openCart();
    await cartPage.expectLoaded();
    await cartPage.removeItemByProductName('Sauce Labs Backpack');
  });

  test('Sort products by highest price', async ({ page }) => {
    const loginPage = new LoginPage(page);
    const productsPage = new ProductsPage(page);

    await loginPage.open();
    await loginPage.login(validUser.username, validUser.password);
    await productsPage.expectLoaded();
    await productsPage.sortByPriceHighToLow();
    await productsPage.expectSortedByHighToLow();
  });

  test('Add Test.allTheThings() T-Shirt to cart and reach checkout information step', async ({ page }) => {
    const loginPage = new LoginPage(page);
    const productsPage = new ProductsPage(page);
    const cartPage = new CartPage(page);
    const checkoutPage = new CheckoutPage(page);

    await loginPage.open();
    await loginPage.login(validUser.username, validUser.password);
    await productsPage.expectLoaded();
    await productsPage.addProductByName('Test.allTheThings() T-Shirt (Red)');
    await productsPage.openCart();
    await cartPage.expectLoaded();
    await cartPage.proceedToCheckout();
    await checkoutPage.expectCustomerInfoVisible();
  });
});
