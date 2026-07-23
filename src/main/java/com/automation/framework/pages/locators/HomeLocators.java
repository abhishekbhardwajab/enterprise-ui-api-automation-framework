package com.automation.framework.pages.locators;

import org.openqa.selenium.By;

/**
 * Locators for the global header/nav present on every automationexercise.com page.
 */
public interface HomeLocators {

    By SIGNUP_LOGIN_LINK = By.cssSelector("a[href='/login']");

    // Rendered as <a href="/logout">Logged in as <b>name</b></a>
    By LOGGED_IN_AS_LINK = By.xpath("//a[contains(.,'Logged in as')]");

    By LOGOUT_LINK = By.cssSelector("a[href='/logout']");

    // Google Consumer Surveys-style overlay renders inside an iframe.
    By SURVEY_IFRAME = By.cssSelector("iframe[src*='survey'], iframe[id*='survey'], iframe[name*='google_ads']");
    By SURVEY_CLOSE_IN_FRAME = By.xpath("//*[self::a or self::button or self::div or self::span][normalize-space(text())='Close']");
}
