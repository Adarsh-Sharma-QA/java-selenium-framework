package com.framework.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Sample page object showing the pattern: locators as fields, behaviour as
 * methods, no assertions in here (assertions belong in step definitions).
 *
 * This targets https://www.saucedemo.com/ - a public demo site built by
 * Sauce Labs specifically for automation practice, so it's safe to use as a
 * throwaway example with no real user or company data involved.
 */
public class LoginPage extends BasePage {

    private final By usernameInput = By.id("user-name");
    private final By passwordInput = By.id("password");
    private final By loginButton = By.id("login-button");
    private final By errorMessage = By.cssSelector("[data-test='error']");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public LoginPage navigateTo(String baseUrl) {
        open(baseUrl);
        return this;
    }

    public void login(String username, String password) {
        type(usernameInput, username);
        type(passwordInput, password);
        click(loginButton);
    }

    public String getErrorMessage() {
        return getText(errorMessage);
    }

    public boolean isErrorDisplayed() {
        return isDisplayed(errorMessage);
    }
}
