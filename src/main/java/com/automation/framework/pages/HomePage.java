package com.automation.framework.pages;

import com.automation.framework.driver.Driver;
import com.automation.framework.pages.locators.HomeLocators;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Task-oriented static methods for the automationexercise.com home page /
 * global header - the most reliable place to assert logged-in vs
 * logged-out state, so both the login and signup flows land here to
 * verify their outcome.
 */
public final class HomePage implements HomeLocators {

    private static final Logger log = LogManager.getLogger(HomePage.class);

    private HomePage() {
    }

    public static void open(String baseUrl) {
        Driver.navigateTo(baseUrl);
    }

    public static void goToSignupLogin() {
        Driver.click(SIGNUP_LOGIN_LINK);
        log.info("Clicked Signup / Login link");
    }

    public static boolean isUserLoggedIn() {
        dismissSurveyOverlayIfPresent();
        return Driver.isDisplayed(LOGGED_IN_AS_LINK);
    }

    public static void dismissSurveyOverlayIfPresent() {
        Driver.dismissIfPresentInFrame(SURVEY_IFRAME, SURVEY_CLOSE_IN_FRAME, 3);
    }

    public static String loggedInAsText() {
        return Driver.text(LOGGED_IN_AS_LINK);
    }

    public static void logout() {
        Driver.click(LOGOUT_LINK);
        log.info("Logged out");
    }
}
