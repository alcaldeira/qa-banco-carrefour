package com.serverest.api.tests;

import com.serverest.api.base.BaseTest;
import com.serverest.api.dto.LoginRequestDTO;
import com.serverest.api.dto.LoginResponseDTO;
import com.serverest.api.dto.MensagemResponseDTO;
import com.serverest.api.dto.UsuarioRequestDTO;
import com.serverest.api.factory.UsuarioFactory;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * POST /login — não faz parte do CRUD de usuários pedido no desafio, mas é a origem do token
 * JWT reaproveitado por {@link com.serverest.api.auth.AuthManager} nos testes de PUT e DELETE.
 * Testado isoladamente para garantir o contrato do fluxo de autenticação.
 */
@Epic("Autenticação")
@Feature("POST /login")
class LoginTest extends BaseTest {

    @Test
    @DisplayName("Deve autenticar com sucesso e retornar um token no formato 'Bearer <jwt>'")
    void deveAutenticarComSucesso() {
        // Arrange
        UsuarioRequestDTO usuario = UsuarioFactory.usuarioValidoAdministrador();
        given().contentType(ContentType.JSON).body(usuario).when().post("/usuarios").then().statusCode(201);
        LoginRequestDTO credenciais = new LoginRequestDTO(usuario.getEmail(), usuario.getPassword());

        // Act
        Response response = given()
                .contentType(ContentType.JSON)
                .body(credenciais)
                .when()
                .post("/login");

        // Assert
        response.then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/login-sucesso-schema.json"));
        LoginResponseDTO corpo = response.as(LoginResponseDTO.class);
        assertEquals("Login realizado com sucesso", corpo.getMessage());
        assertTrue(corpo.getAuthorization().startsWith("Bearer "), "O token deve vir prefixado com 'Bearer '");
    }

    @Test
    @DisplayName("Não deve autenticar com credenciais inválidas (negação de negócio)")
    void naoDeveAutenticarComCredenciaisInvalidas() {
        // Arrange
        LoginRequestDTO credenciaisInvalidas = new LoginRequestDTO("usuario.inexistente@teste.com", "senhaErrada123");

        // Act
        Response response = given()
                .contentType(ContentType.JSON)
                .body(credenciaisInvalidas)
                .when()
                .post("/login");

        // Assert
        response.then()
                .statusCode(401)
                .body(matchesJsonSchemaInClasspath("schemas/mensagem-schema.json"));
        assertEquals("Email e/ou senha inválidos", response.as(MensagemResponseDTO.class).getMessage());
    }

    @Test
    @DisplayName("Não deve autenticar sem informar email e senha")
    void naoDeveAutenticarSemCamposObrigatorios() {
        // Arrange
        LoginRequestDTO credenciaisVazias = new LoginRequestDTO(null, null);

        // Act
        Response response = given()
                .contentType(ContentType.JSON)
                .body(credenciaisVazias)
                .when()
                .post("/login");

        // Assert
        response.then().statusCode(400);
        @SuppressWarnings("unchecked")
        java.util.Map<String, String> erros = response.as(java.util.Map.class);
        assertEquals("email é obrigatório", erros.get("email"));
        assertEquals("password é obrigatório", erros.get("password"));
    }
}
