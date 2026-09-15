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

import java.util.Map;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * GET /usuarios/{id} — consulta de um usuário específico.
 */
@Epic("API de Usuários")
@Feature("GET /usuarios/{id} - Consulta por id")
class GetUsuarioPorIdTest extends BaseTest {

    @Test
    @DisplayName("Deve retornar os dados do usuário quando o id existe")
    @Description("Cria um usuário, busca pelo id retornado e confere os dados e o contrato da resposta")
    void deveBuscarUsuarioPorIdComSucesso() {
        // Arrange
        UsuarioRequestDTO usuarioCriado = UsuarioFactory.usuarioValidoAdministrador();
        CadastroResponseDTO cadastro = UsuariosClient.criar(usuarioCriado).as(CadastroResponseDTO.class);

        // Act
        Response response = UsuariosClient.buscarPorId(cadastro.getId());

        // Assert
        response.then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/usuario-schema.json"));

        UsuarioResponseDTO usuario = response.as(UsuarioResponseDTO.class);
        assertEquals(usuarioCriado.getNome(), usuario.getNome());
        assertEquals(usuarioCriado.getEmail(), usuario.getEmail());
        assertEquals(usuarioCriado.getAdministrador(), usuario.getAdministrador());
        assertEquals(cadastro.getId(), usuario.getId());
    }

    @Test
    @DisplayName("Deve retornar 400 quando o id tem formato válido mas não existe (negação de negócio)")
    @Description("Id com 16 caracteres alfanuméricos (formato aceito), porém nunca cadastrado")
    void deveRetornar400ParaIdInexistente() {
        // Arrange
        String idInexistenteFormatoValido = "aaaaaaaaaaaaaaaa"; // 16 caracteres alfanuméricos

        // Act
        Response response = UsuariosClient.buscarPorId(idInexistenteFormatoValido);

        // Assert
        response.then()
                .statusCode(400)
                .body(matchesJsonSchemaInClasspath("schemas/mensagem-schema.json"));
        MensagemResponseDTO corpo = response.as(MensagemResponseDTO.class);
        assertEquals("Usuário não encontrado", corpo.getMessage());
    }

    @Test
    @DisplayName("Deve retornar 400 quando o id não segue o formato de 16 caracteres alfanuméricos (negação de contrato)")
    @Description("Analogia ao 'CPF inválido': id em formato estruturalmente inválido deve ser rejeitado antes de buscar no banco")
    void deveRetornar400ParaIdComFormatoInvalido() {
        // Arrange
        String idFormatoInvalido = "abc";

        // Act
        Response response = UsuariosClient.buscarPorId(idFormatoInvalido);

        // Assert
        response.then().statusCode(400);
        @SuppressWarnings("unchecked")
        Map<String, String> corpo = response.as(Map.class);
        assertEquals("id deve ter exatamente 16 caracteres alfanuméricos", corpo.get("id"));
    }
}
