package com.automation.framework.pages.locators;

import org.openqa.selenium.By;

/**
 * Locators for the "Enter Account Information" form shown at
 * https://automationexercise.com/signup after submitting name + email.
 */
public interface AccountInformationLocators {

    By TITLE_MR = By.id("id_gender1");
    By PASSWORD_FIELD = By.id("password");
    By DAYS_SELECT = By.id("days");
    By MONTHS_SELECT = By.id("months");
    By YEARS_SELECT = By.id("years");
    By FIRST_NAME_FIELD = By.id("first_name");
    By LAST_NAME_FIELD = By.id("last_name");
    By ADDRESS1_FIELD = By.id("address1");
    By COUNTRY_SELECT = By.id("country");
    By STATE_FIELD = By.id("state");
    By CITY_FIELD = By.id("city");
    By ZIPCODE_FIELD = By.id("zipcode");
    By MOBILE_NUMBER_FIELD = By.id("mobile_number");
    By CREATE_ACCOUNT_BUTTON = By.cssSelector("button[data-qa='create-account']");
}
