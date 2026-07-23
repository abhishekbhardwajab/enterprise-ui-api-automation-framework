package com.automation.tests.stepDefinitions;

import com.automation.framework.context.Constants;
import com.automation.framework.context.UserContext;
import com.automation.framework.context.UserRepository;
import com.automation.framework.model.Account;
import com.automation.framework.pages.AccountCreatedPage;
import com.automation.framework.pages.AccountInformationPage;
import com.automation.framework.pages.LoginPage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;

/**
 * Step definitions for signup.feature.
 * Generates a unique Account (see Account#random) per run so the scenario
 * never collides with a previously-created account, and registers it in
 * UserRepository once created so later steps/scenarios can look it up.
 */
public class SignupSteps {

    private Account account;

    @When("user signs up with a unique name and email")
    public void user_signs_up_with_a_unique_name_and_email() {
        LoginPage.open(Constants.baseUrl());
        account = Account.random();
        LoginPage.signupAs(account.firstName + " " + account.lastName, account.email);
    }

    @And("user completes the account information form")
    public void user_completes_the_account_information_form() {
        AccountInformationPage.completeRegistration(account);
    }

    @Then("the account should be created successfully")
    public void the_account_should_be_created_successfully() {
        Assert.assertTrue("Expected 'Account Created!' confirmation was not shown",
                AccountCreatedPage.isAccountCreatedConfirmed());
        UserRepository.register(account);
        UserContext.setCurrentUser(account);
        AccountCreatedPage.continueToHome();
    }
}
