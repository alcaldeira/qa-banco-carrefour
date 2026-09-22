package com.serverest.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UsuarioRequest {

    private String nome;
    private String email;
    private String password;
    private String administrador;

    public UsuarioRequest() {
    }

    public UsuarioRequest(String nome, String email, String password, String administrador) {
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

        public UsuarioRequest build() {
            return new UsuarioRequest(nome, email, password, administrador);
        }
    }
}
