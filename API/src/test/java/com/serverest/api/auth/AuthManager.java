package com.serverest.api.auth;

import com.serverest.api.factory.UsuarioFactory;
import com.serverest.api.model.LoginRequest;
import com.serverest.api.model.LoginResponse;
import com.serverest.api.model.UsuarioRequest;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public final class AuthManager {

    private static volatile String cachedToken;
    private static final Object LOCK = new Object();

    private AuthManager() {
    }

    public static String getAdminToken() {
        if (cachedToken == null) {
            synchronized (LOCK) {
                if (cachedToken == null) {
                    cachedToken = criarAdministradorEAutenticar();
                }
            }
        }
        return cachedToken;
    }

    public static RequestSpecification authenticatedRequest() {
        return given().header("Authorization", getAdminToken());
    }

    private static String criarAdministradorEAutenticar() {
        UsuarioRequest administrador = UsuarioFactory.usuarioValidoAdministrador();

        given()
                .body(administrador)
        .when()
                .post("/usuarios")
        .then()
                .statusCode(201);

        LoginRequest credenciais = new LoginRequest(administrador.getEmail(), administrador.getPassword());

        LoginResponse login =
                given()
                        .body(credenciais)
                .when()
                        .post("/login")
                .then()
                        .statusCode(200)
                        .extract().as(LoginResponse.class);

        return login.getAuthorization();
    }
}
