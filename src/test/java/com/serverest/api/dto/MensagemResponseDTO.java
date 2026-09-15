package com.serverest.api.dto;

/**
 * DTO genérico para respostas no formato { "message": "..." },
 * usado por PUT (atualização), DELETE e erros de negócio (ex.: e-mail duplicado, id não encontrado).
 */
public class MensagemResponseDTO {

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
