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

import java.util.UUID;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * DELETE /usuarios/{id} — exclusão de usuário, autenticada via token JWT reaproveitável
 * ({@link com.serverest.api.auth.AuthManager}).
 */
@Epic("API de Usuários")
@Feature("DELETE /usuarios/{id} - Exclusão")
class DeleteUsuarioTest extends BaseTest {

    @Test
    @DisplayName("Deve excluir um usuário existente com sucesso, autenticado via JWT")
    @Description("Cria um usuário, exclui pelo id e confirma que ele deixa de existir (GET subsequente retorna 400)")
    void deveExcluirUsuarioExistenteComSucesso() {
        // Arrange
        UsuarioRequestDTO usuario = UsuarioFactory.usuarioValidoAdministrador();
        CadastroResponseDTO cadastro = UsuariosClient.criar(usuario).as(CadastroResponseDTO.class);

        // Act
        Response response = UsuariosClient.deletar(cadastro.getId());

        // Assert
        response.then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/mensagem-schema.json"));
        assertEquals("Registro excluído com sucesso", response.as(MensagemResponseDTO.class).getMessage());

        Response consultaAposExclusao = UsuariosClient.buscarPorId(cadastro.getId());
        consultaAposExclusao.then().statusCode(400);
        assertEquals("Usuário não encontrado", consultaAposExclusao.as(MensagemResponseDTO.class).getMessage());
    }

    @Test
    @DisplayName("Deve retornar 'nenhum registro excluído' quando o id não existe")
    @Description("Negação de negócio: excluir um id inexistente não é erro HTTP, mas retorna mensagem informativa (200)")
    void naoDeveExcluirUsuarioComIdInexistente() {
        // Arrange
        String idInexistente = UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        // Act
        Response response = UsuariosClient.deletar(idInexistente);

        // Assert
        response.then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/mensagem-schema.json"));
        assertEquals("Nenhum registro excluído", response.as(MensagemResponseDTO.class).getMessage());
    }
}
