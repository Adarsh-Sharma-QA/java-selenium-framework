Feature: Login
  As a user of the application
  I want to log in with my credentials
  So that I can access my account

  This is the framework's one sample feature - it exists to demonstrate the
  feature -> step definitions -> page object pattern. Swap the target site
  and locators for your own project's login flow.

  Background:
    Given the user is on the login page

  Scenario: Successful login with valid credentials
    When the user logs in with username "standard_user" and password "secret_sauce"
    Then the user should be redirected to the products page

  Scenario Outline: Unsuccessful login with invalid credentials
    When the user logs in with username "<username>" and password "<password>"
    Then an error message "<message>" should be displayed

    Examples:
      | username         | password       | message                                                        |
      | locked_out_user  | secret_sauce   | Sorry, this user has been locked out.                           |
      | standard_user    | wrong_password | Username and password do not match any user in this service     |
