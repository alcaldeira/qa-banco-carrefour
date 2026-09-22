package com.serverest.api.filters;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

public class RateLimitRetryFilter implements Filter {

    private static final int MAX_RETRIES = 3;
    private static final long BASE_BACKOFF_MS = 1000L;

    @Override
    public Response filter(FilterableRequestSpecification requestSpec,
                            FilterableResponseSpecification responseSpec,
                            FilterContext ctx) {
        Response response = ctx.next(requestSpec, responseSpec);

        int attempt = 0;
        while (response.getStatusCode() == 429 && attempt < MAX_RETRIES) {
            attempt++;
            waitBeforeRetry(attempt);
            response = ctx.next(requestSpec, responseSpec);
        }
        return response;
    }

    private void waitBeforeRetry(int attempt) {
        try {
            Thread.sleep(BASE_BACKOFF_MS * attempt);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
