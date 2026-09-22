package com.serverest.api.base;

import com.serverest.api.filters.RateLimitRetryFilter;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.LogConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;

public abstract class BaseTest {

    protected static final String BASE_URL = System.getProperty("api.base.url", "https://serverest.dev");

    private static volatile boolean configured = false;

    @BeforeAll
    static synchronized void configurarRestAssured() {
        if (configured) {
            return;
        }
        configured = true;

        RestAssured.baseURI = BASE_URL;

        RestAssured.config = RestAssuredConfig.config()
                .logConfig(LogConfig.logConfig().enableLoggingOfRequestAndResponseIfValidationFails());

        RestAssured.filters(
                new AllureRestAssured(),
                new RateLimitRetryFilter()
        );

        RestAssured.requestSpecification = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .build();
    }
}
