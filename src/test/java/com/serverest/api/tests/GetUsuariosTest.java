package com.serverest.api.tests;

import com.serverest.api.base.BaseTest;
import com.serverest.api.clients.UsuariosClient;
import com.serverest.api.dto.UsuarioRequestDTO;
import com.serverest.api.dto.UsuariosListResponseDTO;
import com.serverest.api.factory.UsuarioFactory;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * GET /usuarios — listagem (com e sem filtro por nome).
 */
@Epic("API de Usuários")
@Feature("GET /usuarios - Listagem")
class GetUsuariosTest extends BaseTest {

    @Test
    @DisplayName("Deve listar usuários com status 200 e respeitar o contrato (schema)")
    @Description("Garante o contrato da resposta: {quantidade, usuarios[]} e que 'quantidade' reflete o tamanho da lista")
    void deveListarUsuariosComSucesso() {
        // Arrange
        // A listagem pública já expõe usuários existentes na base; nenhuma massa extra é necessária.

        // Act
        Response response = UsuariosClient.listar();

        // Assert
        response.then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/usuarios-lista-schema.json"));

        UsuariosListResponseDTO corpo = response.as(UsuariosListResponseDTO.class);
        assertEquals(corpo.getQuantidade(), corpo.getUsuarios().size(),
                "O campo 'quantidade' deve refletir o tamanho real da lista de usuários retornada");
    }

    @Test
    @DisplayName("Deve retornar apenas os usuários cujo nome corresponde ao filtro informado")
    @Description("Cria um usuário com nome único e valida que o filtro ?nome= retorna somente ele")
    void deveFiltrarUsuariosPorNome() {
        // Arrange
        UsuarioRequestDTO novoUsuario = UsuarioFactory.usuarioValidoAdministrador();
        UsuariosClient.criar(novoUsuario).then().statusCode(201);

        // Act
        Response response = UsuariosClient.listarPorNome(novoUsuario.getNome());

        // Assert
        response.then().statusCode(200);
        UsuariosListResponseDTO corpo = response.as(UsuariosListResponseDTO.class);
        assertTrue(corpo.getQuantidade() >= 1, "Deveria encontrar ao menos o usuário recém-criado");
        assertTrue(
                corpo.getUsuarios().stream().allMatch(u -> u.getNome().equals(novoUsuario.getNome())),
                "Todos os usuários retornados devem ter exatamente o nome filtrado"
        );
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando nenhum usuário atende ao filtro de nome")
    @Description("Caso de borda: filtro sem correspondência deve retornar quantidade=0 e lista vazia, não erro")
    void deveRetornarListaVaziaParaFiltroSemCorrespondencia() {
        // Arrange
        String nomeInexistente = "NomeQueDefinitivamenteNaoExiste_" + UUID.randomUUID();

        // Act
        Response response = UsuariosClient.listarPorNome(nomeInexistente);

        // Assert
        response.then().statusCode(200);
        UsuariosListResponseDTO corpo = response.as(UsuariosListResponseDTO.class);
        assertEquals(0, corpo.getQuantidade());
        assertTrue(corpo.getUsuarios().isEmpty());
    }
}
