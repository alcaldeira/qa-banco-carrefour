package com.serverest.api.tests;

import com.serverest.api.base.BaseTest;
import com.serverest.api.clients.UsuariosClient;
import com.serverest.api.dto.CadastroResponseDTO;
import com.serverest.api.dto.MensagemResponseDTO;
import com.serverest.api.dto.UsuarioRequestDTO;
import com.serverest.api.dto.UsuarioResponseDTO;
import com.serverest.api.factory.UsuarioFactory;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * PUT /usuarios/{id} — atualização de usuário, autenticada via token JWT reaproveitável
 * ({@link com.serverest.api.auth.AuthManager}).
 */
@Epic("API de Usuários")
@Feature("PUT /usuarios/{id} - Atualização")
class PutUsuarioTest extends BaseTest {

    @Test
    @DisplayName("Deve atualizar um usuário existente com sucesso, autenticado via JWT")
    @Description("Atualiza nome e o campo administrador de um usuário previamente criado e confirma a persistência via GET")
    void deveAtualizarUsuarioExistenteComSucesso() {
        // Arrange
        UsuarioRequestDTO usuarioOriginal = UsuarioFactory.usuarioValidoAdministrador();
        CadastroResponseDTO cadastro = UsuariosClient.criar(usuarioOriginal).as(CadastroResponseDTO.class);

        UsuarioRequestDTO usuarioAtualizado = UsuarioRequestDTO.builder()
                .nome("Nome Atualizado QA")
                .email(usuarioOriginal.getEmail())
                .password(usuarioOriginal.getPassword())
                .administrador("false")
                .build();

        // Act
        Response response = UsuariosClient.atualizar(cadastro.getId(), usuarioAtualizado);

        // Assert
        response.then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/mensagem-schema.json"));
        assertEquals("Registro alterado com sucesso", response.as(MensagemResponseDTO.class).getMessage());

        UsuarioResponseDTO usuarioPersistido = UsuariosClient.buscarPorId(cadastro.getId()).as(UsuarioResponseDTO.class);
        assertEquals("Nome Atualizado QA", usuarioPersistido.getNome());
        assertEquals("false", usuarioPersistido.getAdministrador());
    }

    @Test
    @DisplayName("Deve criar um novo usuário (upsert) quando o id informado no PUT não existe")
    @Description("Regra de negócio particular da API: PUT em um id inexistente cadastra um novo usuário (201), em vez de 404")
    void deveCriarUsuarioAoAtualizarIdInexistente() {
        // Arrange
        String idInexistente = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        UsuarioRequestDTO usuario = UsuarioFactory.usuarioValidoAdministrador();

        // Act
        Response response = UsuariosClient.atualizar(idInexistente, usuario);

        // Assert
        response.then()
                .statusCode(201)
                .body(matchesJsonSchemaInClasspath("schemas/cadastro-sucesso-schema.json"));
        CadastroResponseDTO corpo = response.as(CadastroResponseDTO.class);
        assertEquals("Cadastro realizado com sucesso", corpo.getMessage());
        assertNotNull(corpo.getId());
    }

    @Test
    @DisplayName("Não deve permitir atualização sem os campos obrigatórios")
    @Description("Mesma regra de validação do POST se aplica ao PUT: todos os 4 campos são obrigatórios")
    void naoDeveAtualizarUsuarioSemCamposObrigatorios() {
        // Arrange
        UsuarioRequestDTO usuarioOriginal = UsuarioFactory.usuarioValidoAdministrador();
        CadastroResponseDTO cadastro = UsuariosClient.criar(usuarioOriginal).as(CadastroResponseDTO.class);

        // Act
        Response response = UsuariosClient.atualizar(cadastro.getId(), Collections.emptyMap());

        // Assert
        response.then().statusCode(400);
        @SuppressWarnings("unchecked")
        Map<String, String> erros = response.as(Map.class);
        assertEquals("nome é obrigatório", erros.get("nome"));
        assertEquals("email é obrigatório", erros.get("email"));
        assertEquals("password é obrigatório", erros.get("password"));
        assertEquals("administrador é obrigatório", erros.get("administrador"));
    }

    @Test
    @DisplayName("Não deve permitir atualização com e-mail em formato inválido")
    @Description("Validação de negação: campo email com formato inválido também é rejeitado no PUT")
    void naoDeveAtualizarUsuarioComEmailInvalido() {
        // Arrange
        UsuarioRequestDTO usuarioOriginal = UsuarioFactory.usuarioValidoAdministrador();
        CadastroResponseDTO cadastro = UsuariosClient.criar(usuarioOriginal).as(CadastroResponseDTO.class);

        UsuarioRequestDTO atualizacaoInvalida = UsuarioRequestDTO.builder()
                .nome(usuarioOriginal.getNome())
                .email("email-sem-formato-valido")
                .password(usuarioOriginal.getPassword())
                .administrador(usuarioOriginal.getAdministrador())
                .build();

        // Act
        Response response = UsuariosClient.atualizar(cadastro.getId(), atualizacaoInvalida);

        // Assert
        response.then().statusCode(400);
        @SuppressWarnings("unchecked")
        Map<String, String> erros = response.as(Map.class);
        assertEquals("email deve ser um email válido", erros.get("email"));
    }
}
