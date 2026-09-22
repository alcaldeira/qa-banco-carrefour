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

import java.util.Collections;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Epic("API de Usuários")
@Feature("PUT /usuarios/{id} - Atualização")
@Tag("usuarios")
@Tag("regressivo")
class PutUsuarioTest extends BaseTest {

    @Test
    @Tag("smoke")
    @DisplayName("Deve atualizar um usuário existente com sucesso, autenticado via JWT")
    void deveAtualizarUsuarioExistenteComSucesso() {
        // Arrange
        UsuarioRequest usuarioOriginal = UsuarioFactory.usuarioValidoAdministrador();
        CadastroResponse cadastro =
                given()
                        .body(usuarioOriginal)
                .when()
                        .post("/usuarios")
                .then()
                        .statusCode(201)
                        .extract().as(CadastroResponse.class);

        UsuarioRequest usuarioAtualizado = UsuarioRequest.builder()
                .nome("Nome Atualizado QA")
                .email(usuarioOriginal.getEmail())
                .password(usuarioOriginal.getPassword())
                .administrador("false")
                .build();

        // Act + Assert
        AuthManager.authenticatedRequest()
                .body(usuarioAtualizado)
        .when()
                .put("/usuarios/{id}", cadastro.getId())
        .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/mensagem-schema.json"))
                .body("message", equalTo("Registro alterado com sucesso"));

        given()
        .when()
                .get("/usuarios/{id}", cadastro.getId())
        .then()
                .statusCode(200)
                .body("nome", equalTo("Nome Atualizado QA"))
                .body("administrador", equalTo("false"));
    }

    @Test
    @DisplayName("Deve criar um novo usuário (upsert) quando o id informado no PUT não existe")
    void deveCriarUsuarioAoAtualizarIdInexistente() {
        // Arrange
        String idInexistente = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        UsuarioRequest usuario = UsuarioFactory.usuarioValidoAdministrador();

        // Act + Assert
        AuthManager.authenticatedRequest()
                .body(usuario)
        .when()
                .put("/usuarios/{id}", idInexistente)
        .then()
                .statusCode(201)
                .body(matchesJsonSchemaInClasspath("schemas/cadastro-sucesso-schema.json"))
                .body("message", equalTo("Cadastro realizado com sucesso"))
                .body("_id", notNullValue());
    }

    @Test
    @DisplayName("Não deve permitir atualização sem os campos obrigatórios")
    void naoDeveAtualizarUsuarioSemCamposObrigatorios() {
        // Arrange
        UsuarioRequest usuarioOriginal = UsuarioFactory.usuarioValidoAdministrador();
        CadastroResponse cadastro =
                given()
                        .body(usuarioOriginal)
                .when()
                        .post("/usuarios")
                .then()
                        .statusCode(201)
                        .extract().as(CadastroResponse.class);

        // Act + Assert
        AuthManager.authenticatedRequest()
                .body(Collections.emptyMap())
        .when()
                .put("/usuarios/{id}", cadastro.getId())
        .then()
                .statusCode(400)
                .body("nome", equalTo("nome é obrigatório"))
                .body("email", equalTo("email é obrigatório"))
                .body("password", equalTo("password é obrigatório"))
                .body("administrador", equalTo("administrador é obrigatório"));
    }

    @Test
    @DisplayName("Não deve permitir atualização com e-mail em formato inválido")
    void naoDeveAtualizarUsuarioComEmailInvalido() {
        // Arrange
        UsuarioRequest usuarioOriginal = UsuarioFactory.usuarioValidoAdministrador();
        CadastroResponse cadastro =
                given()
                        .body(usuarioOriginal)
                .when()
                        .post("/usuarios")
                .then()
                        .statusCode(201)
                        .extract().as(CadastroResponse.class);

        UsuarioRequest atualizacaoInvalida = UsuarioRequest.builder()
                .nome(usuarioOriginal.getNome())
                .email("email-sem-formato-valido")
                .password(usuarioOriginal.getPassword())
                .administrador(usuarioOriginal.getAdministrador())
                .build();

        // Act + Assert
        AuthManager.authenticatedRequest()
                .body(atualizacaoInvalida)
        .when()
                .put("/usuarios/{id}", cadastro.getId())
        .then()
                .statusCode(400)
                .body("email", equalTo("email deve ser um email válido"));
    }
}
