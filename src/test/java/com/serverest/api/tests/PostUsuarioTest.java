package com.serverest.api.tests;

import com.serverest.api.base.BaseTest;
import com.serverest.api.factory.UsuarioFactory;
import com.serverest.api.model.UsuarioRequest;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Epic("API de Usuários")
@Feature("POST /usuarios - Criação")
@Tag("usuarios")
@Tag("regressivo")
class PostUsuarioTest extends BaseTest {

    @Test
    @Tag("smoke")
    @DisplayName("Deve criar um usuário administrador com sucesso")
    void deveCriarUsuarioAdministradorComSucesso() {
        // Arrange
        UsuarioRequest novoUsuario = UsuarioFactory.usuarioValidoAdministrador();

        // Act + Assert (Given -> When -> Then)
        given()
                .body(novoUsuario)
        .when()
                .post("/usuarios")
        .then()
                .statusCode(201)
                .body(matchesJsonSchemaInClasspath("schemas/cadastro-sucesso-schema.json"))
                .body("message", equalTo("Cadastro realizado com sucesso"))
                .body("_id", notNullValue());
    }

    @Test
    @DisplayName("Deve criar um usuário não administrador com sucesso")
    void deveCriarUsuarioNaoAdministradorComSucesso() {
        // Arrange
        UsuarioRequest novoUsuario = UsuarioFactory.usuarioValidoNaoAdministrador();

        // Act + Assert
        given()
                .body(novoUsuario)
        .when()
                .post("/usuarios")
        .then()
                .statusCode(201)
                .body(matchesJsonSchemaInClasspath("schemas/cadastro-sucesso-schema.json"));
    }

    @Test
    @DisplayName("Não deve permitir cadastro com e-mail já utilizado por outro usuário")
    void naoDeveCriarUsuarioComEmailDuplicado() {
        // Arrange
        UsuarioRequest usuarioOriginal = UsuarioFactory.usuarioValidoAdministrador();
        given().body(usuarioOriginal).when().post("/usuarios").then().statusCode(201);

        UsuarioRequest usuarioDuplicado = UsuarioRequest.builder()
                .nome("Outro Nome Qualquer")
                .email(usuarioOriginal.getEmail())
                .password("OutraSenha123")
                .administrador("false")
                .build();

        // Act + Assert
        given()
                .body(usuarioDuplicado)
        .when()
                .post("/usuarios")
        .then()
                .statusCode(400)
                .body(matchesJsonSchemaInClasspath("schemas/mensagem-schema.json"))
                .body("message", equalTo("Este email já está sendo usado"));
    }

    @Test
    @DisplayName("Não deve permitir cadastro sem nenhum campo obrigatório informado")
    void naoDeveCriarUsuarioSemCamposObrigatorios() {
        // Act + Assert
        given()
                .body(Collections.emptyMap())
        .when()
                .post("/usuarios")
        .then()
                .statusCode(400)
                .body(matchesJsonSchemaInClasspath("schemas/erro-campos-schema.json"))
                .body("nome", equalTo("nome é obrigatório"))
                .body("email", equalTo("email é obrigatório"))
                .body("password", equalTo("password é obrigatório"))
                .body("administrador", equalTo("administrador é obrigatório"));
    }

    @Test
    @DisplayName("Não deve permitir cadastro com e-mail em formato inválido")
    void naoDeveCriarUsuarioComEmailInvalido() {
        // Arrange
        UsuarioRequest usuario = UsuarioFactory.usuarioComEmailInvalido();

        // Act + Assert
        given()
                .body(usuario)
        .when()
                .post("/usuarios")
        .then()
                .statusCode(400)
                .body("email", equalTo("email deve ser um email válido"));
    }

    @Test
    @DisplayName("Não deve permitir cadastro com valor inválido no campo administrador")
    void naoDeveCriarUsuarioComAdministradorInvalido() {
        // Arrange
        UsuarioRequest usuario = UsuarioFactory.usuarioComAdministradorInvalido();

        // Act + Assert
        given()
                .body(usuario)
        .when()
                .post("/usuarios")
        .then()
                .statusCode(400)
                .body("administrador", equalTo("administrador deve ser 'true' ou 'false'"));
    }
}
