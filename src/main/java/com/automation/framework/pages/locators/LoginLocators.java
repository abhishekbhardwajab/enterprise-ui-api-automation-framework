package com.automation.framework.pages.locators;

import org.openqa.selenium.By;

/**
 * Locators for https://automationexercise.com/login, which hosts two
 * independent forms: "Login to your account" and "New User Signup!".
 * Uses the site's data-qa attributes where available - exposed by the site
 * specifically for automation practice and more stable across markup
 * changes than structural selectors.
 */
public interface LoginLocators {

    // Login form
    By LOGIN_EMAIL_FIELD = By.cssSelector("input[data-qa='login-email1']");
    By LOGIN_PASSWORD_FIELD = By.cssSelector("input[data-qa='login-password']");
    By LOGIN_BUTTON = By.cssSelector("button[data-qa='login-button']");
    By LOGIN_ERROR_MESSAGE = By.xpath("//p[contains(text(),'incorrect')]");

    // Signup form (name/email only - full details captured on AccountInformationPage)
    By SIGNUP_NAME_FIELD = By.cssSelector("input[data-qa='signup-name']");
    By SIGNUP_EMAIL_FIELD = By.cssSelector("input[data-qa='signup-email']");
    By SIGNUP_BUTTON = By.cssSelector("button[data-qa='signup-button']");
    By SIGNUP_ERROR_MESSAGE = By.xpath("//p[contains(text(),'already exist')]");
}
