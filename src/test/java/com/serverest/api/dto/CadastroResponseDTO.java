package com.serverest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO de resposta para criação de usuário: POST /usuarios (201) e
 * PUT /usuarios/{id} quando o id informado não existe e a API faz upsert (201).
 */
public class CadastroResponseDTO {

    private String message;

    @JsonProperty("_id")
    private String id;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
