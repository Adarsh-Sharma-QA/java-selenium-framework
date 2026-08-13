# Java Selenium Framework

A reusable shell for a Java + Selenium WebDriver + Cucumber (BDD) UI test
automation framework. It ships with the plumbing every project needs -
driver management, config loading, a base page object - plus **one** worked
example (feature file, step definitions, page objects) so the pattern is
obvious. It has no project-specific business logic in it; swap in your own
pages, features, and config for a real project.

## Stack

- Java 11
- Selenium WebDriver 4.x
- Cucumber (Gherkin/BDD) 7.x
- JUnit 4 as the runner
- WebDriverManager (auto-downloads the matching chromedriver/geckodriver -
  nothing to check into git)
- Maven

## Structure

```
src/test/java/com/framework/
├── config/
│   └── ConfigReader.java      # loads src/test/resources/config.properties
├── driver/
│   └── DriverFactory.java     # creates/quits WebDriver (Chrome/Firefox), thread-safe
├── pages/
│   ├── BasePage.java          # shared waits/clicks/type helpers
│   ├── LoginPage.java         # sample page object
│   └── ProductsPage.java      # sample second page object
├── stepdefinitions/
│   ├── Hooks.java             # @Before/@After - driver lifecycle, failure screenshots
│   └── LoginSteps.java        # sample step definitions
└── runners/
    └── TestRunner.java        # JUnit + Cucumber entry point

src/test/resources/
├── config.properties
└── features/
    └── login.feature          # sample feature
```

## Running it

```
mvn test
```

This runs `TestRunner`, which executes every `.feature` file under
`src/test/resources/features`. Reports land in `target/cucumber-reports/`.

To watch it run instead of headless, set `headless=false` in
`config.properties`, or override any property from the command line:

```
mvn test -Dheadless=false -Dbrowser=firefox
```

## The sample scenario

`login.feature` runs against https://www.saucedemo.com/, a public site Sauce
Labs built specifically for automation practice - no real user or company
data involved. It's a placeholder: point `base.url` in `config.properties`
at your own application and replace `LoginPage`/`LoginSteps`/`login.feature`
with your own flow.

## Adding a new feature

1. Write the `.feature` file under `src/test/resources/features/`.
2. Add a page object under `pages/` extending `BasePage` (locators as
   fields, actions as methods, no assertions).
3. Add step definitions under `stepdefinitions/` that call the page object
   and assert on the outcome.
4. `mvn test`.

## Notes

- `DriverFactory` uses a `ThreadLocal<WebDriver>`, so scenarios can run in
  parallel (e.g. Cucumber's parallel JUnit runner, or a sharded CI matrix)
  without threads sharing a driver.
- Nothing environment-specific (URLs, credentials, driver binaries) is
  hardcoded - it all comes from `config.properties` or system property
  overrides, so this shell can be dropped into a new project and reconfigured
  without touching framework code.
