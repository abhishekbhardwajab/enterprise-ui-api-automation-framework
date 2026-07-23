package com.automation.tests.apiTests;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

/**
 * API tests for the Users resource.
 *
 * Demonstrates the four validation layers required for enterprise API coverage:
 *   1. Status code
 *   2. Response schema (contract)
 *   3. Business/domain field values
 *   4. Error handling for invalid input
 */
public class UserApiTest extends BaseApiTest {

    @Test(description = "GET /users/{id} returns 200 with a schema-valid payload - status code, schema, and business fields")
    public void getUserById_returnsValidUser() {
        int userId = 1;

        Response response = given()
                .pathParam("id", userId)
                .contentType(ContentType.JSON)
        .when()
                .get("/users/{id}")
        .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/user-schema.json"))
                .extract().response();

        // Business field validations
        Assert.assertEquals(response.jsonPath().getInt("id"), userId, "Returned id should match requested id");
        Assert.assertFalse(response.jsonPath().getString("email").isBlank(), "Email should not be blank");
        Assert.assertTrue(response.jsonPath().getString("email").contains("@"), "Email should be well-formed");
        Assert.assertFalse(response.jsonPath().getString("name").isBlank(), "Name should not be blank");
    }

    @Test(description = "GET /users/{id} for a non-existent id returns 404 - error handling")
    public void getUserById_nonExistentId_returns404() {
        given()
                .pathParam("id", 999999)
        .when()
                .get("/users/{id}")
        .then()
                .statusCode(404);
    }

    @Test(description = "GET /users/{id} with a non-numeric id is rejected without a server error")
    public void getUserById_invalidIdFormat_doesNotServerError() {
        given()
                .pathParam("id", "abc")
        .when()
                .get("/users/{id}")
        .then()
                .statusCode(org.hamcrest.Matchers.lessThan(500));
    }

    @Test(description = "GET /users returns a non-empty collection with expected shape")
    public void getAllUsers_returnsCollection() {
        given()
        .when()
                .get("/users")
        .then()
                .statusCode(200)
                .body("size()", org.hamcrest.Matchers.greaterThan(0))
                .body("[0].id", org.hamcrest.Matchers.notNullValue());
    }
}
