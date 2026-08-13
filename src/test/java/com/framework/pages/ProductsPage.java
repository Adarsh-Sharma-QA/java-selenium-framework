package com.framework.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Sample second page object - shows how pages hand off to one another
 * (LoginPage.login() lands here on success) without step definitions
 * needing to know any locators themselves.
 */
public class ProductsPage extends BasePage {

    private final By pageTitle = By.className("title");

    public ProductsPage(WebDriver driver) {
        super(driver);
    }

    public String getPageTitle() {
        return getText(pageTitle);
    }

    public boolean isLoaded() {
        return currentUrl().contains("inventory.html");
    }
}
