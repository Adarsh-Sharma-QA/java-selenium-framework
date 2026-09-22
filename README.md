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

## Prerequisites

- **JDK 11+** on the `PATH` (`java -version`). The pom compiles to Java 11
  bytecode, so an older JDK will fail the build.
- **Maven 3.6+** (`mvn -v`).
- **The browser itself installed locally.** WebDriverManager downloads the
  *driver* binary (chromedriver/geckodriver), not Chrome or Firefox - if
  neither is installed, driver creation fails with
  `SessionNotCreatedException`.
- **Outbound network access on the first run**, so WebDriverManager can
  resolve and download the driver matching your installed browser version.
  It caches into `~/.cache/selenium`, so subsequent runs work offline.

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

### Running a subset of scenarios by tag

Tag a scenario in a `.feature` file (`@smoke`, `@regression`, etc.) and
filter without touching `TestRunner.java`:

```
mvn test -Dcucumber.filter.tags="@smoke"
```

`cucumber-junit` reads `cucumber.filter.tags` as a system property at
runtime, so this overrides (rather than requires editing) the `tags`
attribute `@CucumberOptions` would otherwise need. Combine tags with
Cucumber's boolean expressions, e.g. `"@smoke and not @wip"` to run smoke
scenarios that aren't still in progress. `login.feature` currently has no
tags, so add one (e.g. `@smoke` above `Scenario:`) before trying this.

## Reports and failure screenshots

`Hooks.java`'s `@After` method checks `scenario.isFailed()` and, if the
driver implements `TakesScreenshot` (both `ChromeDriver` and
`FirefoxDriver` do), grabs a screenshot and calls `scenario.attach(...,
"image/png", ...)` before the driver quits. Cucumber embeds that attachment
into both `target/cucumber-reports/report.html` and `report.json` - open
`report.html` in a browser and expand the failed scenario to see the image
inline. The `pretty` console plugin (also configured in `TestRunner`) does
not render attachments, so a screenshot that exists won't show up in the
`mvn test` console output.

Two situations produce a failed scenario with **no** screenshot attached,
which can look like the feature is broken:

- The failure happened in `@Before` (e.g. `DriverFactory.getDriver()`
  threw while creating the browser) - `tearDown` still runs, but
  `scenario.isFailed()` is true while there's no usable driver yet, so the
  `driver instanceof TakesScreenshot` check aside, the browser never
  reached a page to capture.
- A custom `WebDriver` implementation that doesn't implement
  `TakesScreenshot` was wired into `DriverFactory` - the `instanceof` check
  silently skips the capture rather than throwing.

## Configuration reference

Every key below is read through `ConfigReader`, which checks
`System.getProperty(key)` *first* and only then falls back to
`config.properties` - so any key can be overridden with `-Dkey=value` using
the exact same name, e.g. `-Dimplicit.wait=0`.

| Key | Fallback if unset | Read by | Notes |
| --- | --- | --- | --- |
| `browser` | `chrome` | `DriverFactory.createDriver` | Only `chrome` and `firefox` have branches. Anything else (including a typo like `chrom`) hits the `default:` case and silently launches Chrome. |
| `headless` | `true` | `DriverFactory.createDriver` | Parsed with `Boolean.parseBoolean`, so any value other than `true`/`TRUE` means false. Chrome gets `--headless=new`, Firefox gets `-headless`. |
| `base.url` | *none* | step definitions | The only key with no default - a missing or misspelled entry returns `null` and fails at `driver.get(null)` rather than with a clear config error. |
| `implicit.wait` | `10` | `DriverFactory.createDriver` | Seconds. Parsed with `Integer.parseInt`, so a non-numeric value throws `NumberFormatException` while the driver is being created. |

Two things the file does *not* control:

- **`config.properties` must exist on the classpath even if every key is
  overridden on the command line.** `ConfigReader`'s static initialiser
  throws `IllegalStateException: config.properties not found on classpath`
  before any override is consulted.
- **Chrome's window size.** `DriverFactory` always passes
  `--window-size=1920,1080` (plus `--no-sandbox` and
  `--disable-dev-shm-usage` for containerised CI). This matters headless,
  where the `window().maximize()` call is effectively a no-op - change the
  argument in `DriverFactory` if a scenario needs a different viewport.


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

## Adding a new browser

`DriverFactory.createDriver()` only has branches for `chrome` and `firefox`
(anything else falls through to the `default:` case and launches Chrome -
see the Configuration reference table above). To add Edge:

1. Add the dependency - WebDriverManager already knows how to resolve it,
   nothing extra needed there:
   ```xml
   <dependency>
       <groupId>org.seleniumhq.selenium</groupId>
       <artifactId>selenium-edge-driver</artifactId>
       <version>${selenium.version}</version>
   </dependency>
   ```
2. Add a `case "edge":` branch in `DriverFactory.createDriver()`, mirroring
   the Chrome branch:
   ```java
   case "edge":
       WebDriverManager.edgedriver().setup();
       EdgeOptions edgeOptions = new EdgeOptions();
       if (ConfigReader.headless()) {
           edgeOptions.addArguments("--headless=new");
       }
       driver = new EdgeDriver(edgeOptions);
       break;
   ```
3. Set `browser=edge` in `config.properties` (or pass `-Dbrowser=edge`).

No other class needs to change - `ConfigReader.browser()`, the `ThreadLocal`
lifecycle in `getDriver()`/`quitDriver()`, and the `implicitlyWait()`/
`maximize()` calls after the switch are all browser-agnostic. Note the
headless flag isn't uniform across browsers: Chrome and (new) Edge both
accept `--headless=new`, but Firefox still uses the older single-dash
`-headless` - copy the flag style from the branch you're closest to, not
from a different browser's branch.

## Troubleshooting

**A second runner class doesn't run.** Surefire is pinned to
`**/TestRunner.java` in `pom.xml`, so a new runner (e.g. `SmokeRunner`) is
silently skipped until you add it to the `<includes>` block - or relax the
pattern to `**/*Runner.java`.

**`mvn test` runs scenarios one at a time even though `DriverFactory` is
thread-safe.** The `ThreadLocal` makes parallel execution *safe*, it doesn't
*enable* it. You still need to turn it on - e.g. `cucumber.execution.parallel.enabled=true`
in a `junit-platform.properties` (JUnit 5) or Surefire's `parallel`/`threadCount`
configuration.

**A negative check takes ~15 seconds.** `BasePage.isDisplayed()` swallows the
exception and returns `false`, so "assert this element is *not* there" always
burns the full `WebDriverWait` timeout. For absence checks prefer
`ExpectedConditions.invisibilityOfElementLocated` with a short local wait.

**A failed `@Before` launches a second browser instance, silently.**
`Hooks.tearDown()` calls `DriverFactory.getDriver()` to grab the driver for
the failure-screenshot check - it doesn't just read the existing one. If
`setUp()`'s call to `getDriver()` threw (e.g. `SessionNotCreatedException`
because the installed browser doesn't match the WebDriverManager-downloaded
driver), the `ThreadLocal` was never populated, so `tearDown()`'s call to
`getDriver()` sees `null` and runs `createDriver()` again. If that second
attempt succeeds, a browser window opens and is immediately quit without ever
loading a page - visible as a flash of an extra Chrome/Firefox process in non-
headless runs, or a second slow driver-startup delay in the logs. If it fails
again, the second exception (not the original one) is what surfaces from
`tearDown()`, which can obscure the real root cause. When debugging a
`@Before` failure, check the *first* stack trace in the test output, not
necessarily the last one.

**Timeouts behave unpredictably.** The framework sets both an implicit wait
(`implicit.wait`, applied in `DriverFactory`) and 15s explicit waits (in
`BasePage`). Selenium warns against mixing the two; if you hit odd waiting
behaviour, set `implicit.wait=0` and rely on the explicit waits only.

## Notes

- `DriverFactory` uses a `ThreadLocal<WebDriver>`, so scenarios can run in
  parallel (e.g. Cucumber's parallel JUnit runner, or a sharded CI matrix)
  without threads sharing a driver.
- Nothing environment-specific (URLs, credentials, driver binaries) is
  hardcoded - it all comes from `config.properties` or system property
  overrides, so this shell can be dropped into a new project and reconfigured
  without touching framework code.
