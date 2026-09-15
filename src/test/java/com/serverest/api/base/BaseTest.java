package com.serverest.api.base;

import com.serverest.api.filters.RateLimitRetryFilter;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import io.restassured.config.RestAssuredConfig;
import org.junit.jupiter.api.BeforeAll;

/**
 * Classe base de todos os testes de API. Centraliza a configuração global do RestAssured
 * (URL base, filtros e logging) para que cada classe de teste foque apenas no cenário (AAA),
 * sem duplicar setup.
 */
public abstract class BaseTest {

    protected static final String BASE_URL = System.getProperty("api.base.url", "https://serverest.dev");

    @BeforeAll
    static void configurarRestAssured() {
        RestAssured.baseURI = BASE_URL;

        // Loga request/response automaticamente apenas quando uma assertiva falha,
        // mantendo o log da pipeline legível nos cenários de sucesso.
        RestAssured.config = RestAssuredConfig.config()
                .logConfig(LogConfig.logConfig().enableLoggingOfRequestAndResponseIfValidationFails());

        RestAssured.filters(
                new AllureRestAssured(),      // anexa request/response de cada chamada ao relatório Allure
                new RateLimitRetryFilter()     // reexecuta chamadas que esbarrarem no rate limit (100 req/min)
        );
    }
}
