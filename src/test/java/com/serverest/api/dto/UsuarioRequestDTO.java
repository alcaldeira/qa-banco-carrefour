package com.serverest.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO de requisição usado em POST /usuarios e PUT /usuarios/{id}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UsuarioRequestDTO {

    private String nome;
    private String email;
    private String password;
    private String administrador;

    public UsuarioRequestDTO() {
    }

    public UsuarioRequestDTO(String nome, String email, String password, String administrador) {
        this.nome = nome;
        this.email = email;
        this.password = password;
        this.administrador = administrador;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
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

    public String getAdministrador() {
        return administrador;
    }

    public void setAdministrador(String administrador) {
        this.administrador = administrador;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder fluente: deixa explícito, no Arrange de cada teste, apenas o dado
     * relevante para o cenário (ex.: só o e-mail inválido), sem repetir boilerplate.
     */
    public static class Builder {
        private String nome;
        private String email;
        private String password;
        private String administrador;

        public Builder nome(String nome) {
            this.nome = nome;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder administrador(String administrador) {
            this.administrador = administrador;
            return this;
        }

        public UsuarioRequestDTO build() {
            return new UsuarioRequestDTO(nome, email, password, administrador);
        }
    }
}
