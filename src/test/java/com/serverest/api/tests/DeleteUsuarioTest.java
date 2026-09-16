package com.serverest.api.tests;

import com.serverest.api.auth.AuthManager;
import com.serverest.api.base.BaseTest;
import com.serverest.api.factory.UsuarioFactory;
import com.serverest.api.model.CadastroResponse;
import com.serverest.api.model.UsuarioRequest;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;

@Epic("API de Usuários")
@Feature("DELETE /usuarios/{id} - Exclusão")
@Tag("usuarios")
@Tag("regressivo")
class DeleteUsuarioTest extends BaseTest {

    @Test
    @Tag("smoke")
    @DisplayName("Deve excluir um usuário existente com sucesso, autenticado via JWT")
    void deveExcluirUsuarioExistenteComSucesso() {
        // Arrange
        UsuarioRequest usuario = UsuarioFactory.usuarioValidoAdministrador();
        CadastroResponse cadastro =
                given()
                        .body(usuario)
                .when()
                        .post("/usuarios")
                .then()
                        .statusCode(201)
                        .extract().as(CadastroResponse.class);

        // Act + Assert
        AuthManager.authenticatedRequest()
        .when()
                .delete("/usuarios/{id}", cadastro.getId())
        .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/mensagem-schema.json"))
                .body("message", equalTo("Registro excluído com sucesso"));

        given()
        .when()
                .get("/usuarios/{id}", cadastro.getId())
        .then()
                .statusCode(400)
                .body("message", equalTo("Usuário não encontrado"));
    }

    @Test
    @DisplayName("Deve retornar 'nenhum registro excluído' quando o id não existe")
    void naoDeveExcluirUsuarioComIdInexistente() {
        // Arrange
        String idInexistente = UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        // Act + Assert
        AuthManager.authenticatedRequest()
        .when()
                .delete("/usuarios/{id}", idInexistente)
        .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/mensagem-schema.json"))
                .body("message", equalTo("Nenhum registro excluído"));
    }
}
