package com.automation.framework.pages;

import com.automation.framework.driver.Driver;
import com.automation.framework.pages.locators.LoginLocators;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Task-oriented static methods for https://automationexercise.com/login.
 */
public final class LoginPage implements LoginLocators {

    private static final Logger log = LogManager.getLogger(LoginPage.class);

    private LoginPage() {
    }

    public static void open(String baseUrl) {
        Driver.navigateTo(baseUrl + "/login");
    }

    public static void loginAs(String email, String password) {
        Driver.type(LOGIN_EMAIL_FIELD, email);
        Driver.type(LOGIN_PASSWORD_FIELD, password);
        Driver.click(LOGIN_BUTTON);
        log.info("Submitted login for user [{}]", email);
    }

    public static boolean isLoginErrorDisplayed() {
        return Driver.isDisplayed(LOGIN_ERROR_MESSAGE);
    }

    public static void signupAs(String name, String email) {
        Driver.type(SIGNUP_NAME_FIELD, name);
        Driver.type(SIGNUP_EMAIL_FIELD, email);
        Driver.click(SIGNUP_BUTTON);
        log.info("Submitted initial signup form for [{}] / [{}]", name, email);
    }

    public static boolean isSignupErrorDisplayed() {
        return Driver.isDisplayed(SIGNUP_ERROR_MESSAGE);
    }
}
