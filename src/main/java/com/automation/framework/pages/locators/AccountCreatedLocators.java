package com.automation.framework.pages.locators;

import org.openqa.selenium.By;

/**
 * Locators for the "Account Created!" confirmation screen at
 * https://automationexercise.com/account_created
 */
public interface AccountCreatedLocators {

    By ACCOUNT_CREATED_HEADING = By.xpath("//b[contains(text(),'Account Created!')]");
    By CONTINUE_BUTTON = By.cssSelector("a[data-qa='continue-button']");
}
