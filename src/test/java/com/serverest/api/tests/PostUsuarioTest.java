package com.serverest.api.tests;

import com.serverest.api.base.BaseTest;
import com.serverest.api.clients.UsuariosClient;
import com.serverest.api.dto.CadastroResponseDTO;
import com.serverest.api.dto.MensagemResponseDTO;
import com.serverest.api.dto.UsuarioRequestDTO;
import com.serverest.api.factory.UsuarioFactory;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * POST /usuarios — criação de usuário.
 * Campos obrigatórios: nome, email, password, administrador (string "true"/"false").
 */
@Epic("API de Usuários")
@Feature("POST /usuarios - Criação")
class PostUsuarioTest extends BaseTest {

    @Test
    @DisplayName("Deve criar um usuário administrador com sucesso")
    @Description("Caminho feliz: todos os campos válidos, administrador = 'true'")
    void deveCriarUsuarioAdministradorComSucesso() {
        // Arrange
        UsuarioRequestDTO novoUsuario = UsuarioFactory.usuarioValidoAdministrador();

        // Act
        Response response = UsuariosClient.criar(novoUsuario);

        // Assert
        response.then()
                .statusCode(201)
                .body(matchesJsonSchemaInClasspath("schemas/cadastro-sucesso-schema.json"));

        CadastroResponseDTO corpo = response.as(CadastroResponseDTO.class);
        assertEquals("Cadastro realizado com sucesso", corpo.getMessage());
        assertNotNull(corpo.getId());
        assertEquals(16, corpo.getId().length());
    }

    @Test
    @DisplayName("Deve criar um usuário não administrador com sucesso")
    @Description("Caminho feliz: todos os campos válidos, administrador = 'false'")
    void deveCriarUsuarioNaoAdministradorComSucesso() {
        // Arrange
        UsuarioRequestDTO novoUsuario = UsuarioFactory.usuarioValidoNaoAdministrador();

        // Act
        Response response = UsuariosClient.criar(novoUsuario);

        // Assert
        response.then()
                .statusCode(201)
                .body(matchesJsonSchemaInClasspath("schemas/cadastro-sucesso-schema.json"));
    }

    @Test
    @DisplayName("Não deve permitir cadastro com e-mail já utilizado por outro usuário")
    @Description("Regra de negócio: e-mail é único na base de usuários")
    void naoDeveCriarUsuarioComEmailDuplicado() {
        // Arrange
        UsuarioRequestDTO usuarioOriginal = UsuarioFactory.usuarioValidoAdministrador();
        UsuariosClient.criar(usuarioOriginal).then().statusCode(201);

        UsuarioRequestDTO usuarioDuplicado = UsuarioRequestDTO.builder()
                .nome("Outro Nome Qualquer")
                .email(usuarioOriginal.getEmail())
                .password("OutraSenha123")
                .administrador("false")
                .build();

        // Act
        Response response = UsuariosClient.criar(usuarioDuplicado);

        // Assert
        response.then()
                .statusCode(400)
                .body(matchesJsonSchemaInClasspath("schemas/mensagem-schema.json"));
        MensagemResponseDTO corpo = response.as(MensagemResponseDTO.class);
        assertEquals("Este email já está sendo usado", corpo.getMessage());
    }

    @Test
    @DisplayName("Não deve permitir cadastro sem nenhum campo obrigatório informado")
    @Description("Corpo vazio deve retornar 400 com uma mensagem de validação por campo obrigatório")
    void naoDeveCriarUsuarioSemCamposObrigatorios() {
        // Arrange
        Map<String, Object> corpoVazio = Collections.emptyMap();

        // Act
        Response response = UsuariosClient.criar(corpoVazio);

        // Assert
        response.then()
                .statusCode(400)
                .body(matchesJsonSchemaInClasspath("schemas/erro-campos-schema.json"));

        @SuppressWarnings("unchecked")
        Map<String, String> erros = response.as(Map.class);
        assertEquals("nome é obrigatório", erros.get("nome"));
        assertEquals("email é obrigatório", erros.get("email"));
        assertEquals("password é obrigatório", erros.get("password"));
        assertEquals("administrador é obrigatório", erros.get("administrador"));
    }

    @Test
    @DisplayName("Não deve permitir cadastro com e-mail em formato inválido")
    @Description("Validação de negação análoga a 'CPF inválido': campo com formato estruturalmente incorreto")
    void naoDeveCriarUsuarioComEmailInvalido() {
        // Arrange
        UsuarioRequestDTO usuario = UsuarioFactory.usuarioComEmailInvalido();

        // Act
        Response response = UsuariosClient.criar(usuario);

        // Assert
        response.then().statusCode(400);
        @SuppressWarnings("unchecked")
        Map<String, String> erros = response.as(Map.class);
        assertEquals("email deve ser um email válido", erros.get("email"));
    }

    @Test
    @DisplayName("Não deve permitir cadastro com valor inválido no campo administrador")
    @Description("Campo 'administrador' só aceita os valores 'true' ou 'false' (validação de domínio)")
    void naoDeveCriarUsuarioComAdministradorInvalido() {
        // Arrange
        UsuarioRequestDTO usuario = UsuarioFactory.usuarioComAdministradorInvalido();

        // Act
        Response response = UsuariosClient.criar(usuario);

        // Assert
        response.then().statusCode(400);
        @SuppressWarnings("unchecked")
        Map<String, String> erros = response.as(Map.class);
        assertEquals("administrador deve ser 'true' ou 'false'", erros.get("administrador"));
    }
}
