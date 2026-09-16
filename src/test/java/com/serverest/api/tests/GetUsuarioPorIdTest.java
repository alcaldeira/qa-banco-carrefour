package com.serverest.api.tests;

import com.serverest.api.base.BaseTest;
import com.serverest.api.factory.UsuarioFactory;
import com.serverest.api.model.CadastroResponse;
import com.serverest.api.model.UsuarioRequest;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;

@Epic("API de Usuários")
@Feature("GET /usuarios/{id} - Consulta por id")
@Tag("usuarios")
@Tag("regressivo")
class GetUsuarioPorIdTest extends BaseTest {

    @Test
    @Tag("smoke")
    @DisplayName("Deve retornar os dados do usuário quando o id existe")
    void deveBuscarUsuarioPorIdComSucesso() {
        // Arrange
        UsuarioRequest usuarioCriado = UsuarioFactory.usuarioValidoAdministrador();
        CadastroResponse cadastro =
                given()
                        .body(usuarioCriado)
                .when()
                        .post("/usuarios")
                .then()
                        .statusCode(201)
                        .extract().as(CadastroResponse.class);

        // Act + Assert (Given -> When -> Then)
        given()
        .when()
                .get("/usuarios/{id}", cadastro.getId())
        .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/usuario-schema.json"))
                .body("nome", equalTo(usuarioCriado.getNome()))
                .body("email", equalTo(usuarioCriado.getEmail()))
                .body("administrador", equalTo(usuarioCriado.getAdministrador()))
                .body("_id", equalTo(cadastro.getId()));
    }

    @Test
    @DisplayName("Deve retornar 400 quando o id tem formato válido mas não existe (negação de negócio)")
    void deveRetornar400ParaIdInexistente() {
        // Arrange
        String idInexistenteFormatoValido = "aaaaaaaaaaaaaaaa"; // 16 caracteres alfanuméricos

        // Act + Assert
        given()
        .when()
                .get("/usuarios/{id}", idInexistenteFormatoValido)
        .then()
                .statusCode(400)
                .body(matchesJsonSchemaInClasspath("schemas/mensagem-schema.json"))
                .body("message", equalTo("Usuário não encontrado"));
    }

    @Test
    @DisplayName("Deve retornar 400 quando o id não segue o formato de 16 caracteres alfanuméricos (negação de contrato)")
    void deveRetornar400ParaIdComFormatoInvalido() {
        // Arrange
        String idFormatoInvalido = "abc";

        // Act + Assert
        given()
        .when()
                .get("/usuarios/{id}", idFormatoInvalido)
        .then()
                .statusCode(400)
                .body("id", equalTo("id deve ter exatamente 16 caracteres alfanuméricos"));
    }
}
