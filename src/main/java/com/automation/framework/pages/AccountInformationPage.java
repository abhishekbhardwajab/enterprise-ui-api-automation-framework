package com.automation.framework.pages;

import com.automation.framework.driver.Driver;
import com.automation.framework.model.Account;
import com.automation.framework.pages.locators.AccountInformationLocators;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Task-oriented static methods for the "Enter Account Information" form
 * shown at https://automationexercise.com/signup after submitting name + email.
 */
public final class AccountInformationPage implements AccountInformationLocators {

    private static final Logger log = LogManager.getLogger(AccountInformationPage.class);

    private AccountInformationPage() {
    }

    /**
     * Fills in and submits the full registration form from an Account domain object.
     */
    public static void completeRegistration(Account account) {
        Driver.click(TITLE_MR);
        Driver.type(PASSWORD_FIELD, account.password);

        Driver.selectByValue(DAYS_SELECT, "10");
        Driver.select(MONTHS_SELECT, "May");
        Driver.selectByValue(YEARS_SELECT, "1990");

        Driver.type(FIRST_NAME_FIELD, account.firstName);
        Driver.type(LAST_NAME_FIELD, account.lastName);
        Driver.type(ADDRESS1_FIELD, account.address);

        Driver.select(COUNTRY_SELECT, "United States");

        Driver.type(STATE_FIELD, account.state);
        Driver.type(CITY_FIELD, account.city);
        Driver.type(ZIPCODE_FIELD, account.zipcode);
        Driver.type(MOBILE_NUMBER_FIELD, account.mobileNumber);

        Driver.click(CREATE_ACCOUNT_BUTTON);
        log.info("Submitted account information for [{}] [{}]", account.firstName, account.lastName);
    }
}
