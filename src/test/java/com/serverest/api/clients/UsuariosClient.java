package com.serverest.api.clients;

import com.serverest.api.auth.AuthManager;
import com.serverest.api.dto.UsuarioRequestDTO;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

/**
 * Wrapper único para todas as chamadas HTTP dos endpoints de usuário. Concentrar as chamadas
 * aqui evita duplicar RestAssured "cru" em cada teste e mantém o passo "Act" de cada cenário
 * em uma única linha legível.
 */
public final class UsuariosClient {

    private static final String RECURSO = "/usuarios";

    private UsuariosClient() {
    }

    public static Response listar() {
        return given()
                .contentType(ContentType.JSON)
                .when()
                .get(RECURSO);
    }

    public static Response listarPorNome(String nome) {
        return given()
                .contentType(ContentType.JSON)
                .queryParam("nome", nome)
                .when()
                .get(RECURSO);
    }

    public static Response buscarPorId(String id) {
        return given()
                .contentType(ContentType.JSON)
                .when()
                .get(RECURSO + "/{id}", id);
    }

    public static Response criar(UsuarioRequestDTO usuario) {
        return criar((Object) usuario);
    }

    /** Sobrecarga usada nos testes de validação, onde o corpo precisa ser "cru" (ex.: {}). */
    public static Response criar(Object corpo) {
        return given()
                .contentType(ContentType.JSON)
                .body(corpo)
                .when()
                .post(RECURSO);
    }

    public static Response atualizar(String id, UsuarioRequestDTO usuario) {
        return atualizar(id, (Object) usuario);
    }

    public static Response atualizar(String id, Object corpo) {
        return AuthManager.authenticatedRequest()
                .body(corpo)
                .when()
                .put(RECURSO + "/{id}", id);
    }

    public static Response deletar(String id) {
        return AuthManager.authenticatedRequest()
                .when()
                .delete(RECURSO + "/{id}", id);
    }
}
