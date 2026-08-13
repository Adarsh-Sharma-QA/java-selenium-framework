package com.framework.stepdefinitions;

import com.framework.config.ConfigReader;
import com.framework.driver.DriverFactory;
import com.framework.pages.LoginPage;
import com.framework.pages.ProductsPage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Sample step definitions wiring login.feature to the page objects.
 * This is the one end-to-end example the shell ships with - copy this
 * pattern (feature -> steps -> page object) for new features.
 */
public class LoginSteps {

    private final LoginPage loginPage = new LoginPage(DriverFactory.getDriver());
    private final ProductsPage productsPage = new ProductsPage(DriverFactory.getDriver());

    @Given("the user is on the login page")
    public void the_user_is_on_the_login_page() {
        loginPage.navigateTo(ConfigReader.baseUrl());
    }

    @When("the user logs in with username {string} and password {string}")
    public void the_user_logs_in_with_username_and_password(String username, String password) {
        loginPage.login(username, password);
    }

    @Then("the user should be redirected to the products page")
    public void the_user_should_be_redirected_to_the_products_page() {
        assertTrue("Expected to land on the products page after login", productsPage.isLoaded());
        assertEquals("Products", productsPage.getPageTitle());
    }

    @Then("an error message {string} should be displayed")
    public void an_error_message_should_be_displayed(String expectedMessage) {
        assertTrue("Expected an error message to be displayed", loginPage.isErrorDisplayed());
        assertTrue(
                "Expected error text to contain: " + expectedMessage,
                loginPage.getErrorMessage().contains(expectedMessage)
        );
    }
}
