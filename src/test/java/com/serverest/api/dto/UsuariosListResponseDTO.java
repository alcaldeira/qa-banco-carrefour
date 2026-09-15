package com.serverest.api.dto;

import java.util.List;

/**
 * DTO de resposta de GET /usuarios: { quantidade, usuarios: [...] }.
 */
public class UsuariosListResponseDTO {

    private int quantidade;
    private List<UsuarioResponseDTO> usuarios;

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public List<UsuarioResponseDTO> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(List<UsuarioResponseDTO> usuarios) {
        this.usuarios = usuarios;
    }
}
