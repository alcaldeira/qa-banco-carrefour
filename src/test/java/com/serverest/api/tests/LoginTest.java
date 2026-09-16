package com.serverest.api.tests;

import com.serverest.api.base.BaseTest;
import com.serverest.api.factory.UsuarioFactory;
import com.serverest.api.model.LoginRequest;
import com.serverest.api.model.UsuarioRequest;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;

@Epic("Autenticação")
@Feature("POST /login")
@Tag("login")
@Tag("regressivo")
class LoginTest extends BaseTest {

    @Test
    @Tag("smoke")
    @DisplayName("Deve autenticar com sucesso e retornar um token no formato 'Bearer <jwt>'")
    void deveAutenticarComSucesso() {
        // Arrange
        UsuarioRequest usuario = UsuarioFactory.usuarioValidoAdministrador();
        given().body(usuario).when().post("/usuarios").then().statusCode(201);
        LoginRequest credenciais = new LoginRequest(usuario.getEmail(), usuario.getPassword());

        // Act + Assert (Given -> When -> Then)
        given()
                .body(credenciais)
        .when()
                .post("/login")
        .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/login-sucesso-schema.json"))
                .body("message", equalTo("Login realizado com sucesso"))
                .body("authorization", startsWith("Bearer "));
    }

    @Test
    @DisplayName("Não deve autenticar com credenciais inválidas (negação de negócio)")
    void naoDeveAutenticarComCredenciaisInvalidas() {
        // Arrange
        LoginRequest credenciaisInvalidas = new LoginRequest("usuario.inexistente@teste.com", "senhaErrada123");

        // Act + Assert
        given()
                .body(credenciaisInvalidas)
        .when()
                .post("/login")
        .then()
                .statusCode(401)
                .body(matchesJsonSchemaInClasspath("schemas/mensagem-schema.json"))
                .body("message", equalTo("Email e/ou senha inválidos"));
    }

    @Test
    @DisplayName("Não deve autenticar sem informar email e senha")
    void naoDeveAutenticarSemCamposObrigatorios() {
        // Arrange
        LoginRequest credenciaisVazias = new LoginRequest(null, null);

        // Act + Assert
        given()
                .body(credenciaisVazias)
        .when()
                .post("/login")
        .then()
                .statusCode(400)
                .body("email", equalTo("email é obrigatório"))
                .body("password", equalTo("password é obrigatório"));
    }
}
