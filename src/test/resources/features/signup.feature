@ui @signup
Feature: New User Registration
  As a visitor to automationexercise.com
  I want to create a new account
  So that I can shop and manage orders under my own login

  # Fully self-contained: generates a unique email/name each run, so this
  # is safe to include in every CI run without colliding with prior data.
  @smoke
  Scenario: New user should be able to register an account successfully
    Given user navigates to the login page
    When user signs up with a unique name and email
    And user completes the account information form
    Then the account should be created successfully
    And the user should be logged in successfully
