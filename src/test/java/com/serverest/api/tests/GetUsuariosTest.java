package com.serverest.api.tests;

import com.serverest.api.base.BaseTest;
import com.serverest.api.factory.UsuarioFactory;
import com.serverest.api.model.UsuarioRequest;
import com.serverest.api.model.UsuariosListResponse;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Epic("API de Usuários")
@Feature("GET /usuarios - Listagem")
@Tag("usuarios")
@Tag("regressivo")
class GetUsuariosTest extends BaseTest {

    @Test
    @Tag("smoke")
    @DisplayName("Deve listar usuários com status 200 e respeitar o contrato (schema)")
    void deveListarUsuariosComSucesso() {
        // Act + Assert (Given -> When -> Then)
        UsuariosListResponse corpo =
                given()
                .when()
                        .get("/usuarios")
                .then()
                        .statusCode(200)
                        .body(matchesJsonSchemaInClasspath("schemas/usuarios-lista-schema.json"))
                        .extract().as(UsuariosListResponse.class);

        assertEquals(corpo.getQuantidade(), corpo.getUsuarios().size(),
                "O campo 'quantidade' deve refletir o tamanho real da lista de usuários retornada");
    }

    @Test
    @DisplayName("Deve retornar apenas os usuários cujo nome corresponde ao filtro informado")
    void deveFiltrarUsuariosPorNome() {
        // Arrange
        UsuarioRequest novoUsuario = UsuarioFactory.usuarioValidoAdministrador();
        given()
                .body(novoUsuario)
        .when()
                .post("/usuarios")
        .then()
                .statusCode(201);

        // Act + Assert
        UsuariosListResponse corpo =
                given()
                        .queryParam("nome", novoUsuario.getNome())
                .when()
                        .get("/usuarios")
                .then()
                        .statusCode(200)
                        .extract().as(UsuariosListResponse.class);

        assertTrue(corpo.getQuantidade() >= 1, "Deveria encontrar ao menos o usuário recém-criado");
        assertTrue(
                corpo.getUsuarios().stream().allMatch(u -> u.getNome().equals(novoUsuario.getNome())),
                "Todos os usuários retornados devem ter exatamente o nome filtrado"
        );
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando nenhum usuário atende ao filtro de nome")
    void deveRetornarListaVaziaParaFiltroSemCorrespondencia() {
        // Arrange
        String nomeInexistente = "NomeQueDefinitivamenteNaoExiste_" + UUID.randomUUID();

        // Act + Assert
        given()
                .queryParam("nome", nomeInexistente)
        .when()
                .get("/usuarios")
        .then()
                .statusCode(200)
                .body("quantidade", equalTo(0))
                .body("usuarios", empty());
    }
}
