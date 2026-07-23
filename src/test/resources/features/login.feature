@ui @login
Feature: User Login Validation
  As a registered user of automationexercise.com
  I want to log into the application
  So that I can access my account

  # Deterministic, self-contained - always safe to run in CI as it requires no pre-existing account.
  @smoke @negative
  Scenario: Invalid credentials should be rejected
    Given user navigates to the login page
    When user enters invalid credentials
    Then an invalid login error should be displayed

  # Requires a real, pre-registered automationexercise.com account.
  # Register one manually (or run signup.feature once) and set valid.username/valid.password
  # in src/test/resources/config/<env>.properties, then include this tag explicitly:
  #   mvn test -Dcucumber.filter.tags="@requires-account"
  @requires-account
  Scenario: Valid user should login successfully
    Given user navigates to the login page
    When user enters valid credentials
    Then the user should be logged in successfully
