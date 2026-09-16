package com.serverest.api.factory;

import com.serverest.api.model.UsuarioRequest;

import java.util.UUID;

public final class UsuarioFactory {

    private static final String SENHA_PADRAO = "Teste@123";

    private UsuarioFactory() {
    }

    public static UsuarioRequest usuarioValidoAdministrador() {
        String identificador = UUID.randomUUID().toString();
        return UsuarioRequest.builder()
                .nome("QA Automation " + identificador.substring(0, 8))
                .email("qa.automation." + identificador + "@teste.com")
                .password(SENHA_PADRAO)
                .administrador("true")
                .build();
    }

    public static UsuarioRequest usuarioValidoNaoAdministrador() {
        String identificador = UUID.randomUUID().toString();
        return UsuarioRequest.builder()
                .nome("QA Automation " + identificador.substring(0, 8))
                .email("qa.automation." + identificador + "@teste.com")
                .password(SENHA_PADRAO)
                .administrador("false")
                .build();
    }

    public static UsuarioRequest usuarioComEmailInvalido() {
        UsuarioRequest base = usuarioValidoAdministrador();
        return UsuarioRequest.builder()
                .nome(base.getNome())
                .email("email-sem-formato-valido")
                .password(base.getPassword())
                .administrador(base.getAdministrador())
                .build();
    }

    public static UsuarioRequest usuarioComAdministradorInvalido() {
        UsuarioRequest base = usuarioValidoAdministrador();
        return UsuarioRequest.builder()
                .nome(base.getNome())
                .email(base.getEmail())
                .password(base.getPassword())
                .administrador("sim")
                .build();
    }
}
