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
