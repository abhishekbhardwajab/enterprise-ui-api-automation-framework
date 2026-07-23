package com.automation.tests.apiTests;

import com.automation.framework.config.ConfigReader;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import org.testng.annotations.BeforeClass;

/**
 * Shared Rest Assured configuration for all API test classes: base URI
 * from application.properties, plus request/response logging so every
 * call is traceable in the Surefire/console output.
 */
public abstract class BaseApiTest {

    @BeforeClass
    public void baseSetup() {
        RestAssured.baseURI = ConfigReader.get("api.base.url");
        RestAssured.filters(
                new RequestLoggingFilter(),
                new ResponseLoggingFilter()
        );
    }
}
