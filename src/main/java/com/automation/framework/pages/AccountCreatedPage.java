package com.automation.framework.pages;

import com.automation.framework.driver.Driver;
import com.automation.framework.pages.locators.AccountCreatedLocators;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Task-oriented static methods for the "Account Created!" confirmation
 * screen at https://automationexercise.com/account_created
 */
public final class AccountCreatedPage implements AccountCreatedLocators {

    private static final Logger log = LogManager.getLogger(AccountCreatedPage.class);

    private AccountCreatedPage() {
    }

    public static boolean isAccountCreatedConfirmed() {
        return Driver.isDisplayed(ACCOUNT_CREATED_HEADING);
    }

    public static void continueToHome() {
        Driver.click(CONTINUE_BUTTON);
        log.info("Clicked continue after account creation");
    }
}
