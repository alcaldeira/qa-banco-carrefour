package com.serverest.api.model;

import java.util.List;

public class UsuariosListResponse {

    private int quantidade;
    private List<UsuarioResponse> usuarios;

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public List<UsuarioResponse> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(List<UsuarioResponse> usuarios) {
        this.usuarios = usuarios;
    }
}
