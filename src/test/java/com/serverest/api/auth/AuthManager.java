package com.serverest.api.auth;

import com.serverest.api.dto.LoginRequestDTO;
import com.serverest.api.dto.LoginResponseDTO;
import com.serverest.api.dto.UsuarioRequestDTO;
import com.serverest.api.factory.UsuarioFactory;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

/**
 * Componente reaproveitável de autenticação JWT (requisito: "autenticação é feita via token JWT").
 * <p>
 * Fluxo: cria (uma única vez por execução) um usuário administrador via POST /usuarios,
 * autentica via POST /login e mantém o token em cache para ser reutilizado por todos os
 * testes que precisem enviar o header Authorization (PUT e DELETE).
 * <p>
 * Observação de contrato: na API pública ServeRest, PUT/DELETE de /usuarios não rejeitam
 * chamadas sem token (não há verificação de autorização nessas rotas). Mesmo assim, a suíte
 * envia o Bearer token em todas as chamadas mutáveis, seguindo a prática esperada por uma API
 * autenticada via JWT, conforme descrito no requisito do desafio.
 */
public final class AuthManager {

    private static volatile String cachedToken;
    private static final Object LOCK = new Object();

    private AuthManager() {
    }

    public static String getAdminToken() {
        if (cachedToken == null) {
            synchronized (LOCK) {
                if (cachedToken == null) {
                    cachedToken = criarAdministradorEAutenticar();
                }
            }
        }
        return cachedToken;
    }

    /** Retorna uma RequestSpecification já pronta com Content-Type JSON e o header Authorization. */
    public static RequestSpecification authenticatedRequest() {
        return given()
                .contentType(ContentType.JSON)
                .header("Authorization", getAdminToken());
    }

    private static String criarAdministradorEAutenticar() {
        UsuarioRequestDTO administrador = UsuarioFactory.usuarioValidoAdministrador();

        given()
                .contentType(ContentType.JSON)
                .body(administrador)
                .when()
                .post("/usuarios")
                .then()
                .statusCode(201);

        LoginRequestDTO credenciais = new LoginRequestDTO(administrador.getEmail(), administrador.getPassword());

        LoginResponseDTO login = given()
                .contentType(ContentType.JSON)
                .body(credenciais)
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .extract()
                .as(LoginResponseDTO.class);

        return login.getAuthorization();
    }
}
