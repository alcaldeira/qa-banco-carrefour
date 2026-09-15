package com.serverest.api.dto;

/**
 * DTO de resposta de POST /login em caso de sucesso: contém o token JWT
 * (campo "authorization", já no formato "Bearer &lt;token&gt;").
 */
public class LoginResponseDTO {

    private String message;
    private String authorization;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAuthorization() {
        return authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }
}
