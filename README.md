# Enterprise UI + API Automation Framework

A Java browser-automation framework for automationexercise.com, combining
Cucumber BDD execution (launched through JUnit 4 runner classes), reusable
page objects with a locator/model/context layering, CSV-backed test data,
local and BrowserStack cloud execution, CSV/HTML/text reporting, and a
Jenkins CI pipeline with failed-item rerun. A separate Rest Assured/TestNG
layer covers API testing and sits outside that core design.

## Tech Stack

| Layer            | Tool |
|------------------|------|
| Language         | Java 11 |
| Build            | Apache Maven |
| BDD              | Cucumber, launched via JUnit 4 runner classes |
| UI Automation    | Selenium WebDriver |
| Structured data  | Apache Commons CSV |
| Mailbox access   | Jakarta Mail (IMAP) |
| MFA/TOTP         | dev.samstevens.totp |
| API Testing      | Rest Assured + TestNG (separate layer, see below) |
| CI               | Jenkins (declarative pipeline) |
| Remote browsers  | BrowserStack |
| Notifications    | Slack |
| Logging          | Log4j2 / SLF4J |

## Repository Layout

```
enterprise-ui-api-automation-framework/
├── src
│   ├── main/java/com/automation/framework
│   │   ├── driver/       Driver.java              - shared static facade: creation,
│   │   │                                             waits, JS exec, tab switching, shutdown,
│   │   │                                             local-vs-BrowserStack selection
│   │   ├── pages/        static page classes, grouped by application area
│   │   │   └── locators/ *Locators interfaces - Selenium By values only
│   │   ├── model/        domain data objects and enums (Account, Title)
│   │   ├── context/      UserContext (current user), UserRepository (CSV-backed),
│   │   │                 Constants (env -> URL map)
│   │   ├── utils/        CsvUtils, StringUtils, MailUtils, TotpUtil, ScreenshotUtil,
│   │   │                 TestDataGenerator
│   │   ├── exceptions/   ViewNotLoadedException
│   │   └── config/       ConfigReader (application.properties)
│   │
│   └── test
│       ├── java/com/automation/tests
│       │   ├── runners/          CucumberRunnerTest, CucumberRerunTest,
│       │   │                     CucumberDryRunTest, CucumberSmokeRunnerTest
│       │   ├── stepDefinitions/  Hooks.java, LoginSteps.java, SignupSteps.java
│       │   ├── plugins/          CsvReportListener.java (custom Cucumber report plugin)
│       │   ├── apiTests/         BaseApiTest.java, UserApiTest.java (TestNG/Rest Assured)
│       │   └── listeners/        RetryAnalyzer.java, RetryListener.java (API layer only)
│       │
│       └── resources
│           ├── features/         login.feature, signup.feature
│           ├── data/             users.csv (seed accounts for UserRepository)
│           ├── schemas/          user-schema.json (API response contract)
│           ├── application.properties
│           └── testng.xml        API suite only - see "Two Execution Models" below
│
├── reports/            generated: screenshots, logs (gitignored)
├── target/             generated: HTML/JSON/CSV reports, rerun.txt (gitignored)
├── pom.xml
├── Jenkinsfile
└── README.md
```

## Architecture

1. **Locator layer** - interfaces under `pages/locators` ending in `Locators`, holding
   `By` constants only. Keeping selectors separate from interaction logic isolates UI
   changes to one file per page.

2. **Page layer** - classes under `pages` implement their locator interface and expose
   task-oriented **static** methods (`LoginPage.loginAs(...)`, `HomePage.isUserLoggedIn()`).
   All browser access routes through the static `Driver` facade rather than an
   instance-held WebDriver, so pages carry no state of their own.

3. **Domain-model layer** - plain objects in `model` (`Account`, `Title` enum) represent
   application data passed into pages or reconstructed from CSV rows. `Account` uses
   public fields, `equals`/`hashCode`/`toString` on email, and a `random()` factory for
   disposable test data.

4. **Context and data layer** - `UserContext` holds the currently authenticated account
   (ThreadLocal-backed, cleared by hooks between scenarios). `UserRepository` loads
   `data/users.csv` into an in-memory map at class initialization and can register
   newly created accounts (e.g. from the signup flow) at runtime.

5. **Binding and orchestration layer** - Cucumber step-definition classes coordinate
   pages, models, and context. `Hooks` initializes the browser and clears
   `UserContext` before each scenario, then captures a screenshot / reports
   BrowserStack status / tears the driver down afterward.

### Two Execution Models

The UI layer (Cucumber/JUnit4) and the API layer (Rest Assured/TestNG) are kept
separate rather than forced into one Surefire execution:

```bash
mvn test                # UI: runs the default JUnit4 Cucumber runner
mvn test -Papi           # API: runs testng.xml (UserApiTest)
```

This is a deliberate departure for this repo, which covers both UI and API testing;
a single-purpose UI framework wouldn't need the `api` profile at all.

## Browser Interaction and Synchronization

All browser access goes through `Driver`'s static methods: `click`, `type`, `text`,
`isDisplayed`, `select`/`selectByValue`, `executeJs`, `switchToNewWindow`. Waits are
explicit (`WebDriverWait` + `ExpectedConditions`); a timed-out wait is converted into
a descriptive `ViewNotLoadedException` rather than surfacing Selenium's generic
`TimeoutException`, so failures are easier to diagnose from a stack trace alone.

## Execution Model

Maven Surefire selects a Cucumber runner class through the `runnerClass` property
(default `CucumberRunnerTest`). Three more runner classes cover failed-run replay,
dry-run validation, and a targeted local smoke run:

```bash
mvn test                                              # default full suite
mvn test -DrunnerClass=CucumberDryRunTest \
         -Dcucumber.execution.dry-run=true             # validate step bindings, no browser
mvn test -DrunnerClass=CucumberSmokeRunnerTest          # @smoke tag only, fast local check
mvn test -PfailedTests                                  # rerun target/rerun.txt from the last run
```

Supported Maven execution modes:

```bash
mvn test                            # default local execution (headless Chrome)
mvn test -Premote                   # remote execution via BrowserStack
mvn test -PfailedTests              # rerun items from the previous failed-run file
mvn test -Pparallel                 # bounded 2-thread parallel execution
mvn test -PparallelUnlimited        # unlimited-thread parallel execution
mvn -DskipTests package             # compile and package without browser execution
mvn test -Papi                      # API-testing layer (TestNG/Rest Assured)
```

`parallel`/`parallelUnlimited` run against per-feature runner classes generated at
build time by `cucumber-jvm-parallel-plugin` (bound to `generate-test-sources`),
rather than the single aggregate runner - each generated class is an independent
JUnit4 test, which is what lets Surefire's `parallel=classes` distribute them
across threads safely (the `Driver` ThreadLocal keeps each thread's browser session
isolated).

## Runtime Configuration

`src/test/resources/application.properties` defines non-secret defaults: the target
environment identifier (`env`), headless-browser behavior (`headless`), and
BrowserStack build/project naming. Any key can be overridden per-run via a matching
`-Dkey=value` without editing the file - this is how the Maven profiles and Jenkins
pipeline communicate execution mode to the driver/runner infrastructure.
Environment identifiers resolve to a base URL via `context.Constants` (all four
point at the same public site here, standing in for what would otherwise be
distinct deployments).

BrowserStack credentials are never read from a properties file: `Driver` reads
`BROWSERSTACK_USERNAME` / `BROWSERSTACK_ACCESS_KEY` from the environment, injected
by Jenkins credentials binding.

## Data and Utility Support

- **CsvUtils** - reads a header-based CSV resource into a list of column-name/value
  row maps (`UserRepository`'s seed data); appends generated records to a file on
  disk, writing the header row on first write.
- **StringUtils** - splits comma-separated or newline-separated values into trimmed lists.
- **MailUtils** - connects to an IMAP mailbox, polls for a recent message matching a
  subject filter, parses multipart content, and extracts the first URL found (e.g. a
  verification link). Not currently exercised by a feature here - automationexercise.com
  has no email-verification step - but included as a reusable capability.
- **TotpUtil** - generates a TOTP enrollment secret and the currently valid code for
  it. Same status as MailUtils: available, not currently exercised by a live flow.
- **TestDataGenerator** - unique email/mobile/zipcode generation, used by `Account.random()`.

## UI Target: automationexercise.com

Two feature files, two different reliability profiles:

- **`signup.feature`** *(tagged `@smoke`)* - fully self-contained. Builds a
  disposable `Account` via `Account.random()` every run and drives the complete
  registration flow: initial signup form → account information form → confirmation
  → verifies the user lands logged in, then registers the account in
  `UserRepository`. Safe to run in CI on every build.

- **`login.feature`**
  - *`Invalid credentials should be rejected`* (tagged `@smoke @negative`) - also
    self-contained; a bad email/password always produces the site's error message.
  - *`Valid user should login successfully`* (tagged `@requires-account`) - needs a
    **real, pre-registered** automationexercise.com account. Register one manually
    (or run `signup.feature` once and reuse those details), set
    `valid.username`/`valid.password` in `application.properties` (or inject via
    `-D` from a CI secret), then opt in explicitly:
    ```bash
    mvn test -Dcucumber.filter.tags="@requires-account"
    ```
    Excluded from the default run (`CucumberRunnerTest`'s tag filter is
    `not @wip and not @requires-account`) so the suite stays green without manual setup.

Locators use the site's `data-qa` attributes where available (added by the site
specifically for automation practice); if the site's markup changes, update the
corresponding `By` constants under `pages/locators`.

## Reporting and Generated Artifacts

- HTML report - `target/cucumber-reports/cucumber.html`
- JSON report (machine-readable, feeds Jenkins' final-state calculation) -
  `target/cucumber-reports/cucumber.json`
- Failed-item path file, used by `mvn test -PfailedTests` - `target/rerun.txt`
- CSV report detail (one row per finished scenario: timestamp/name/status/duration),
  written by the custom `CsvReportListener` plugin -
  `target/cucumber-reports/scenario-results.csv`
- Console/text summary - Cucumber's `pretty` plugin output
- BrowserStack session recordings and public build link - retrieved by the Jenkins
  pipeline via the BrowserStack API, not generated locally

Everything under `target/` and `reports/` is generated and gitignored.

## Continuous Integration

The Jenkins declarative pipeline runs remote (BrowserStack) or local browser
automation in two stages: an initial run, followed by a failed-item rerun. It
starts/stops the BrowserStack local tunnel, injects credentials through Jenkins'
credential store, retrieves the public BrowserStack build link via their API,
parses the generated JSON summaries, calculates a final build state
(`success` / `failed` / `unstable` / `skipped` / `passed-on-rerun`), and posts a
threaded Slack notification with that status plus the build/BrowserStack links.
Missing or unparsable summaries fall back to `unstable` with a warning rather than
crashing the pipeline outright.

## Security and Maintainability Notes

- BrowserStack and Slack credentials are injected via Jenkins credential bindings
  and environment variables - never hardcoded in source.
- `data/users.csv` here holds only placeholder seed data; real generated-credential
  output must stay untracked (`.gitignore`) and access-controlled.
- Static page methods and the ThreadLocal `Driver`/`UserContext` state are what make
  parallel execution safe - if either were changed to non-thread-local statics,
  parallel profiles would no longer be safe to use.
- `MailUtils` and `TotpUtil` are available but unexercised by any current feature;
  wiring them into a real SSO/MFA or email-verification flow would need an
  application that actually has one.

## Common Build Commands

```bash
mvn test                                                # default UI runner
mvn test -Premote                                       # BrowserStack execution
mvn test -PfailedTests                                  # rerun failed-run file
mvn test -Pparallel                                      # 2-thread parallel
mvn test -PparallelUnlimited                              # unlimited-thread parallel
mvn test -Papi                                           # API-testing layer
mvn -DskipTests package                                   # compile/package only
```

## Extension Conventions

- Keep reusable browser interactions as static methods on `Driver`, not on pages.
- Keep Selenium selectors in a matching `*Locators` interface - never inline in a page class.
- Represent transferable UI data with `model` classes and enums.
- Use `UserContext`/`UserRepository` only for state that must be reset by `Hooks` between scenarios.
- Keep `application.properties` non-secret; inject credentials via environment
  variables or CI secret storage only.
- Preserve Java 11 compatibility and four-space indentation.
