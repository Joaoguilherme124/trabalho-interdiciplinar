package com.example.trabalhointerdiciplinar.data.preferencias;

import com.example.trabalhointerdiciplinar.data.simulacao.Armazenamento;

import java.util.LinkedHashSet;
import java.util.Set;

/** Produtos favoritos por usuário, guardados localmente. Contas diferentes não se misturam. */
public final class Favoritos {
    private static final String PREFIXO = "favoritos_";
    private static final String SEP = ",";
    private final Armazenamento armazenamento;

    public Favoritos(Armazenamento armazenamento) {
        this.armazenamento = armazenamento;
    }

    public synchronized Set<String> ids(String usuarioId) {
        Set<String> ids = new LinkedHashSet<>();
        if (usuarioId == null) return ids;
        String bruto = armazenamento.ler(PREFIXO + usuarioId);
        if (bruto == null || bruto.isEmpty()) return ids;
        for (String id : bruto.split(SEP)) {
            if (!id.isEmpty()) ids.add(id);
        }
        return ids;
    }

    public synchronized boolean eFavorito(String usuarioId, String produtoId) {
        return ids(usuarioId).contains(produtoId);
    }

    /** Inverte o estado e devolve o novo (true = favorito). Sem usuário, nada é salvo. */
    public synchronized boolean alternar(String usuarioId, String produtoId) {
        if (usuarioId == null || produtoId == null) return false;
        Set<String> ids = ids(usuarioId);
        boolean agora = !ids.remove(produtoId);
        if (agora) ids.add(produtoId);
        armazenamento.gravar(PREFIXO + usuarioId, String.join(SEP, ids));
        return agora;
    }
}
