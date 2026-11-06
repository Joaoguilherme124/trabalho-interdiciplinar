package com.example.trabalhointerdiciplinar.ui.buyer;

/** Seções da tela de Informações do Evento que podem ser abertas diretamente. */
public enum SecaoEvento {
    TOPO,
    ABADEUS,
    PROGRAMACAO,
    DUVIDAS;

    /** Converte o extra recebido; valor ausente ou desconhecido abre no topo. */
    public static SecaoEvento daString(String nome) {
        if (nome == null) return TOPO;
        for (SecaoEvento s : values()) {
            if (s.name().equals(nome)) return s;
        }
        return TOPO;
    }
}
