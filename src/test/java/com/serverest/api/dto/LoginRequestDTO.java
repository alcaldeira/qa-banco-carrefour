package com.serverest.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO de requisição para POST /login. Campos nulos são omitidos na serialização
 * (em vez de virarem "campo": null) para simular corretamente a ausência do campo,
 * já que a API distingue "campo ausente" ("é obrigatório") de "campo nulo" ("deve ser uma string").
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginRequestDTO {

    private String email;
    private String password;

    public LoginRequestDTO() {
    }

    public LoginRequestDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
