package com.automation.tests.stepDefinitions;

import com.automation.framework.config.ConfigReader;
import com.automation.framework.context.Constants;
import com.automation.framework.pages.HomePage;
import com.automation.framework.pages.LoginPage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;

/**
 * Step definitions for login.feature.
 * Holds only orchestration + assertions; all element interaction lives in the static Page classes.
 */
public class LoginSteps {

    @Given("user navigates to the login page")
    public void user_navigates_to_the_login_page() {
        LoginPage.open(Constants.baseUrl());
    }

    @When("user enters valid credentials")
    public void user_enters_valid_credentials() {
        LoginPage.loginAs(
                ConfigReader.get("valid.username"),
                ConfigReader.get("valid.password"));
    }

    @When("user enters invalid credentials")
    public void user_enters_invalid_credentials() {
        LoginPage.loginAs("nonexistent.qe.user@example.com", "WrongPassword123!");
    }

    @Then("the user should be logged in successfully")
    public void the_user_should_be_logged_in_successfully() {
        Assert.assertTrue("Expected 'Logged in as' header to be displayed after login",
                HomePage.isUserLoggedIn());
    }

    @Then("an invalid login error should be displayed")
    public void an_invalid_login_error_should_be_displayed() {
        Assert.assertTrue("Expected login error message was not shown",
                LoginPage.isLoginErrorDisplayed());
    }
}
