package com.serverest.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CadastroResponse {

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
