package com.serverest.api.factory;

import com.serverest.api.dto.UsuarioRequestDTO;

import java.util.UUID;

/**
 * Fábrica de massa de dados para os testes de usuário. Gera sempre e-mails únicos
 * (a base da ServeRest é pública e compartilhada entre todos os candidatos/usuários,
 * então reutilizar um e-mail fixo causaria colisões e testes instáveis em CI).
 */
public final class UsuarioFactory {

    private static final String SENHA_PADRAO = "Teste@123";

    private UsuarioFactory() {
    }

    public static UsuarioRequestDTO usuarioValidoAdministrador() {
        String identificador = UUID.randomUUID().toString();
        return UsuarioRequestDTO.builder()
                .nome("QA Automation " + identificador.substring(0, 8))
                .email("qa.automation." + identificador + "@teste.com")
                .password(SENHA_PADRAO)
                .administrador("true")
                .build();
    }

    public static UsuarioRequestDTO usuarioValidoNaoAdministrador() {
        String identificador = UUID.randomUUID().toString();
        return UsuarioRequestDTO.builder()
                .nome("QA Automation " + identificador.substring(0, 8))
                .email("qa.automation." + identificador + "@teste.com")
                .password(SENHA_PADRAO)
                .administrador("false")
                .build();
    }

    public static UsuarioRequestDTO usuarioComEmailInvalido() {
        UsuarioRequestDTO base = usuarioValidoAdministrador();
        return UsuarioRequestDTO.builder()
                .nome(base.getNome())
                .email("email-sem-formato-valido")
                .password(base.getPassword())
                .administrador(base.getAdministrador())
                .build();
    }

    public static UsuarioRequestDTO usuarioComAdministradorInvalido() {
        UsuarioRequestDTO base = usuarioValidoAdministrador();
        return UsuarioRequestDTO.builder()
                .nome(base.getNome())
                .email(base.getEmail())
                .password(base.getPassword())
                .administrador("sim") // valor fora do domínio esperado ('true' | 'false')
                .build();
    }
}
